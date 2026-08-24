package com.scheduler.backend.job;

import com.scheduler.backend.job.dto.CreateJobRequest;
import com.scheduler.backend.job.dto.JobResponse;
import com.scheduler.backend.jobexecution.JobExecution;
import com.scheduler.backend.jobexecution.JobExecutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scheduler.backend.queue.Queue;
import com.scheduler.backend.queue.QueueRepository;
import com.scheduler.backend.retrypolicy.RetryPolicy;
import com.scheduler.backend.retrypolicy.RetryPolicyRepository;
import com.scheduler.backend.dlq.DeadLetterJob;
import com.scheduler.backend.dlq.DeadLetterJobRepository;
import com.scheduler.backend.auth.User;
import com.scheduler.backend.auth.UserRepository;
import com.scheduler.backend.config.SecurityUtils;
import com.scheduler.backend.project.Project;
import com.scheduler.backend.project.ProjectRepository;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final JobExecutionRepository jobExecutionRepository;
    private final QueueRepository queueRepository;
    private final RetryPolicyRepository retryPolicyRepository;
    private final DeadLetterJobRepository deadLetterJobRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public JobService(
            JobRepository jobRepository,
            JobExecutionRepository jobExecutionRepository,
            QueueRepository queueRepository,
            RetryPolicyRepository retryPolicyRepository,
            DeadLetterJobRepository deadLetterJobRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository
    ) {
        this.jobRepository = jobRepository;
        this.jobExecutionRepository = jobExecutionRepository;
        this.queueRepository = queueRepository;
        this.retryPolicyRepository = retryPolicyRepository;
        this.deadLetterJobRepository = deadLetterJobRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    // ============================================================
    // CREATE JOB
    // ============================================================

    @Transactional
    public JobResponse createJob(CreateJobRequest request) {

        Queue queue = getOwnedQueue(request.getQueueId());

        // The project supplied by the client must match the queue's project.
        if (!queue.getProjectId().equals(request.getProjectId())) {
            throw new AccessDeniedException(
                    "Queue does not belong to the specified project"
            );
        }

        Job job = new Job();

        job.setProjectId(queue.getProjectId());
        job.setQueueId(queue.getId());
        job.setJobType(request.getJobType());
        job.setPayload(request.getPayload());
        job.setPriority(request.getPriority());

        job.setStatus(JobStatus.PENDING);
        job.setRetryCount(0);

        // Inherit retry policy from queue.
        if (queue.getRetryPolicyId() != null) {

            RetryPolicy retryPolicy =
                    retryPolicyRepository.findById(
                            queue.getRetryPolicyId()
                    ).orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Retry policy not found: "
                                            + queue.getRetryPolicyId()
                            )
                    );

            // Defensive project check.
            if (!retryPolicy.getProjectId()
                    .equals(queue.getProjectId())) {

                throw new AccessDeniedException(
                        "Retry policy does not belong to this project"
                );
            }

            job.setRetryPolicyId(retryPolicy.getId());
            job.setMaxRetries(retryPolicy.getMaxRetries());

        } else {

            job.setMaxRetries(request.getMaxRetries());
        }

        return JobResponse.from(
                jobRepository.save(job)
        );
    }

    // ============================================================
    // GET JOB
    // ============================================================

    @Transactional(readOnly = true)
    public JobResponse getJob(Long id) {

        Job job = jobRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Job not found: " + id
                        )
                );

        getOwnedQueue(job.getQueueId());

        return JobResponse.from(job);
    }

    // ============================================================
    // GET JOBS BY QUEUE
    // ============================================================

    @Transactional(readOnly = true)
    public List<JobResponse> getJobs(Long queueId) {

        getOwnedQueue(queueId);

        return jobRepository
                .findByQueueId(queueId)
                .stream()
                .map(JobResponse::from)
                .toList();
    }

    public void verifyJobOwnership(Long jobId) {

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Job not found: " + jobId
                        )
                );

        getOwnedQueue(job.getQueueId());
    }

    // ============================================================
    // EXECUTE JOB
    // ============================================================

    @Transactional
    public Job executeJob(Long jobId, Long workerId) {

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Job not found: " + jobId
                        )
                );

        // --------------------------------------------------------
        // Verify worker owns the job
        // --------------------------------------------------------

        if (job.getWorkerId() == null ||
                !job.getWorkerId().equals(workerId)) {

            throw new RuntimeException(
                    "Job is not claimed by this worker"
            );
        }

        // --------------------------------------------------------
        // Job must be CLAIMED before execution
        // --------------------------------------------------------

        if (job.getStatus() != JobStatus.CLAIMED) {

            throw new RuntimeException(
                    "Job must be CLAIMED before execution"
            );
        }

        // --------------------------------------------------------
        // Start execution
        // --------------------------------------------------------

        Instant start = Instant.now();

        job.setStatus(JobStatus.RUNNING);
        job.setStartedAt(start);

        jobRepository.save(job);

        // --------------------------------------------------------
        // Create execution history record
        // --------------------------------------------------------

        JobExecution execution = new JobExecution();

        execution.setJobId(job.getId());
        execution.setWorkerId(workerId);

        int attemptNumber = job.getRetryCount() + 1;

        execution.setAttemptNumber(attemptNumber);
        execution.setStatus("RUNNING");
        execution.setStartedAt(start);

        execution = jobExecutionRepository.save(execution);

        // ========================================================
        // ACTUAL JOB EXECUTION
        // ========================================================

        try {

            /*
             * TEST_FAIL is used to simulate a failed job.
             *
             * This is useful for testing retry behaviour.
             */
            if ("TEST_FAIL".equalsIgnoreCase(job.getJobType())) {

                throw new RuntimeException(
                        "Simulated job failure"
                );
            }

            /*
             * Temporary simulated execution.
             *
             * Later this section can be replaced with
             * actual job handlers.
             */
            Thread.sleep(100);

            // ----------------------------------------------------
            // Job succeeded
            // ----------------------------------------------------

            Instant completed = Instant.now();

            job.setStatus(JobStatus.SUCCESS);
            job.setCompletedAt(completed);

            execution.setStatus("SUCCESS");
            execution.setCompletedAt(completed);

            execution.setDurationMs(
                    completed.toEpochMilli()
                            - start.toEpochMilli()
            );

            jobRepository.save(job);
            jobExecutionRepository.save(execution);

            return job;

        } catch (InterruptedException e) {

            /*
             * Restore interrupt flag.
             */
            Thread.currentThread().interrupt();

            return handleFailure(
                    job,
                    execution,
                    start,
                    e.getMessage()
            );

        } catch (RuntimeException e) {

            return handleFailure(
                    job,
                    execution,
                    start,
                    e.getMessage()
            );
        }
    }

    // ============================================================
    // HANDLE FAILED JOB
    // ============================================================

    private Job handleFailure(
            Job job,
            JobExecution execution,
            Instant start,
            String errorMessage
    ) {

        Instant completed = Instant.now();

        // --------------------------------------------------------
        // Increment retry count
        // --------------------------------------------------------

        int newRetryCount = job.getRetryCount() + 1;
        job.setRetryCount(newRetryCount);

        // --------------------------------------------------------
        // Update execution history
        // --------------------------------------------------------

        execution.setStatus("FAILED");
        execution.setCompletedAt(completed);
        execution.setErrorMessage(errorMessage);

        execution.setDurationMs(
                completed.toEpochMilli()
                        - start.toEpochMilli()
        );

        // ========================================================
        // RETRY AVAILABLE
        // ========================================================

        if (newRetryCount < job.getMaxRetries()) {

            /*
             * Temporary exponential backoff.
             *
             * Retry 1 -> 2 seconds
             * Retry 2 -> 4 seconds
             * Retry 3 -> 8 seconds
             *
             * We will replace this with the RetryPolicy
             * configuration in the next step.
             */
            long delayMs = calculateRetryDelay(
                    job,
                    newRetryCount
            );

            job.setStatus(JobStatus.PENDING);

            job.setAvailableAt(
                    completed.plusMillis(delayMs)
            );

            // Clear worker assignment
            job.setWorkerId(null);
            job.setClaimedAt(null);
            job.setStartedAt(null);
            job.setCompletedAt(null);

        } else {

            // ====================================================
            // NO RETRIES LEFT
            // ====================================================

            job.setStatus(JobStatus.FAILED);
            job.setCompletedAt(completed);

            // Worker is no longer responsible for this job
            job.setWorkerId(null);
            job.setClaimedAt(null);

            // ====================================================
            // MOVE JOB TO DEAD LETTER QUEUE
            // ====================================================

            DeadLetterJob deadLetterJob = new DeadLetterJob();

            deadLetterJob.setJobId(job.getId());
            deadLetterJob.setReason("MAX_RETRIES_EXCEEDED");
            deadLetterJob.setFinalError(errorMessage);
            deadLetterJob.setRetryCount(newRetryCount);

            deadLetterJobRepository.save(deadLetterJob);
        }

        // --------------------------------------------------------
        // Save changes
        // --------------------------------------------------------

        jobRepository.save(job);
        jobExecutionRepository.save(execution);

        return job;
    }

    private long calculateRetryDelay(
            Job job,
            int retryNumber
    ) {

        // No retry policy configured for this job
        if (job.getRetryPolicyId() == null) {

            return Math.min(
                    1000L * (1L << Math.min(retryNumber - 1, 20)),
                    60000L
            );
        }

        RetryPolicy policy =
                retryPolicyRepository.findById(
                        job.getRetryPolicyId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Retry policy not found: "
                                        + job.getRetryPolicyId()
                        )
                );

        long initialDelay = policy.getInitialDelayMs();
        long maxDelay = policy.getMaxDelayMs();

        String backoffType =
                policy.getBackoffType().toUpperCase();

        long delay;

        switch (backoffType) {

            case "FIXED":
                delay = initialDelay;
                break;

            case "LINEAR":
                delay = initialDelay * retryNumber;
                break;

            case "EXPONENTIAL":
                long multiplier =
                        1L << Math.min(retryNumber - 1, 20);

                delay = initialDelay * multiplier;
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported backoff type: "
                                + backoffType
                );
        }

        return Math.min(delay, maxDelay);
    }

    private User getCurrentUser() {

        Long userId = SecurityUtils.getCurrentUserId();

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user not found"
                        )
                );
    }

    private Project getOwnedProject(Long projectId) {

        User user = getCurrentUser();

        Long organizationId = user.getOrganizationId();

        if (organizationId == null) {
            throw new AccessDeniedException(
                    "User is not assigned to an organization"
            );
        }

        return projectRepository
                .findByIdAndOrganizationId(
                        projectId,
                        organizationId
                )
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "You do not have access to this project"
                        )
                );
    }

    private Queue getOwnedQueue(Long queueId) {

        Queue queue = queueRepository.findById(queueId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Queue not found: " + queueId
                        )
                );

        getOwnedProject(queue.getProjectId());

        return queue;
    }
}