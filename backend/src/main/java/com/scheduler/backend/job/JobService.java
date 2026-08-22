package com.scheduler.backend.job;

import com.scheduler.backend.job.dto.CreateJobRequest;
import com.scheduler.backend.job.dto.JobResponse;
import com.scheduler.backend.jobexecution.JobExecution;
import com.scheduler.backend.jobexecution.JobExecutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final JobExecutionRepository jobExecutionRepository;

    public JobService(
            JobRepository jobRepository,
            JobExecutionRepository jobExecutionRepository
    ) {
        this.jobRepository = jobRepository;
        this.jobExecutionRepository = jobExecutionRepository;
    }

    // ============================================================
    // CREATE JOB
    // ============================================================

    @Transactional
    public JobResponse createJob(CreateJobRequest request) {

        Job job = new Job();

        job.setProjectId(request.getProjectId());
        job.setQueueId(request.getQueueId());
        job.setJobType(request.getJobType());
        job.setPayload(request.getPayload());
        job.setPriority(request.getPriority());
        job.setMaxRetries(request.getMaxRetries());

        job.setStatus(JobStatus.PENDING);
        job.setRetryCount(0);

        Job savedJob = jobRepository.save(job);

        return JobResponse.from(savedJob);
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

        return JobResponse.from(job);
    }

    // ============================================================
    // GET JOBS BY QUEUE
    // ============================================================

    @Transactional(readOnly = true)
    public List<JobResponse> getJobs(Long queueId) {

        return jobRepository
                .findByQueueId(queueId)
                .stream()
                .map(JobResponse::from)
                .toList();
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

        int newRetryCount =
                job.getRetryCount() + 1;

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
             * Exponential backoff:
             *
             * Retry 1 -> 2 seconds
             * Retry 2 -> 4 seconds
             * Retry 3 -> 8 seconds
             * ...
             */
            long delaySeconds =
                    (long) Math.pow(2, newRetryCount);

            job.setStatus(JobStatus.PENDING);

            job.setAvailableAt(
                    completed.plusSeconds(delaySeconds)
            );

            /*
             * Clear worker assignment so another worker
             * can claim the job.
             */
            job.setWorkerId(null);
            job.setClaimedAt(null);
            job.setStartedAt(null);
            job.setCompletedAt(null);

        }

        // ========================================================
        // NO RETRIES LEFT
        // ========================================================

        else {

            job.setStatus(JobStatus.FAILED);

            job.setCompletedAt(completed);
        }

        // --------------------------------------------------------
        // Save changes
        // --------------------------------------------------------

        jobRepository.save(job);
        jobExecutionRepository.save(execution);

        return job;
    }
}