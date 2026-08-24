package com.scheduler.backend.dlq;

import com.scheduler.backend.auth.User;
import com.scheduler.backend.auth.UserRepository;
import com.scheduler.backend.config.SecurityUtils;
import com.scheduler.backend.dlq.dto.DeadLetterJobResponse;
import com.scheduler.backend.job.Job;
import com.scheduler.backend.job.JobRepository;
import com.scheduler.backend.job.JobStatus;
import com.scheduler.backend.project.ProjectRepository;
import com.scheduler.backend.queue.Queue;
import com.scheduler.backend.queue.QueueRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class DeadLetterJobService {

    private final DeadLetterJobRepository deadLetterJobRepository;
    private final JobRepository jobRepository;
    private final QueueRepository queueRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public DeadLetterJobService(
            DeadLetterJobRepository deadLetterJobRepository,
            JobRepository jobRepository,
            QueueRepository queueRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository
    ) {
        this.deadLetterJobRepository = deadLetterJobRepository;
        this.jobRepository = jobRepository;
        this.queueRepository = queueRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
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

    private void verifyJobOwnership(Job job) {

        Queue queue = queueRepository.findById(job.getQueueId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Queue not found: " + job.getQueueId()
                        )
                );

        User user = getCurrentUser();

        Long organizationId = user.getOrganizationId();

        if (organizationId == null) {
            throw new AccessDeniedException(
                    "User is not assigned to an organization"
            );
        }

        projectRepository
                .findByIdAndOrganizationId(
                        queue.getProjectId(),
                        organizationId
                )
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "You do not have access to this job"
                        )
                );
    }

    private DeadLetterJob getOwnedDlqJob(Long id) {

        DeadLetterJob dlqJob =
                deadLetterJobRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "DLQ entry not found: " + id
                                )
                        );

        Job job =
                jobRepository.findById(dlqJob.getJobId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Job not found: "
                                                + dlqJob.getJobId()
                                )
                        );

        verifyJobOwnership(job);

        return dlqJob;
    }

    @Transactional(readOnly = true)
    public List<DeadLetterJobResponse> getAll() {

        return deadLetterJobRepository.findAll()
                .stream()
                .filter(dlqJob -> {

                    try {
                        Job job =
                                jobRepository.findById(
                                        dlqJob.getJobId()
                                ).orElse(null);

                        if (job == null) {
                            return false;
                        }

                        verifyJobOwnership(job);
                        return true;

                    } catch (AccessDeniedException e) {
                        return false;
                    }
                })
                .map(DeadLetterJobResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DeadLetterJobResponse getById(Long id) {

        DeadLetterJob dlqJob = getOwnedDlqJob(id);

        return DeadLetterJobResponse.from(dlqJob);
    }

    @Transactional
    public Job requeue(Long id) {

        DeadLetterJob dlqJob = getOwnedDlqJob(id);

        Job job =
                jobRepository.findById(dlqJob.getJobId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Job not found: "
                                                + dlqJob.getJobId()
                                )
                        );

        job.setStatus(JobStatus.PENDING);
        job.setRetryCount(0);

        job.setWorkerId(null);
        job.setClaimedAt(null);
        job.setStartedAt(null);
        job.setCompletedAt(null);

        job.setAvailableAt(Instant.now());

        return jobRepository.save(job);
    }
}