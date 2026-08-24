package com.scheduler.backend.queue;

import com.scheduler.backend.auth.User;
import com.scheduler.backend.auth.UserRepository;
import com.scheduler.backend.config.SecurityUtils;
import com.scheduler.backend.job.JobRepository;
import com.scheduler.backend.job.JobStatus;
import com.scheduler.backend.queue.dto.CreateQueueRequest;
import com.scheduler.backend.queue.dto.QueueResponse;
import com.scheduler.backend.queue.dto.QueueStatsResponse;
import com.scheduler.backend.queue.dto.UpdateQueueRequest;
import com.scheduler.backend.project.Project;
import com.scheduler.backend.project.ProjectRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scheduler.backend.retrypolicy.RetryPolicyRepository;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

@Service
public class QueueService {

    private final QueueRepository queueRepository;
    private final JobRepository jobRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final RetryPolicyRepository retryPolicyRepository;

    public QueueService(
            QueueRepository queueRepository,
            JobRepository jobRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            RetryPolicyRepository retryPolicyRepository
    ) {
        this.queueRepository = queueRepository;
        this.jobRepository = jobRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.retryPolicyRepository = retryPolicyRepository;
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
                                "Queue not found"
                        )
                );

        getOwnedProject(queue.getProjectId());

        return queue;
    }

    @Transactional
    public QueueResponse createQueue(
            CreateQueueRequest request
    ) {

        getOwnedProject(request.getProjectId());

        if (queueRepository.existsByProjectIdAndName(
                request.getProjectId(),
                request.getName()
        )) {
            throw new IllegalArgumentException(
                    "Queue already exists for this project"
            );
        }

        Queue queue = new Queue();

        queue.setProjectId(request.getProjectId());
        queue.setName(request.getName());
        queue.setPriority(request.getPriority());
        queue.setMaxConcurrency(request.getMaxConcurrency());
        queue.setProjectId(request.getProjectId());
        queue.setName(request.getName());
        queue.setPriority(request.getPriority());
        queue.setMaxConcurrency(request.getMaxConcurrency());

        if (request.getRetryPolicyId() != null) {

            retryPolicyRepository
                    .findByIdAndProjectId(
                            request.getRetryPolicyId(),
                            request.getProjectId()
                    )
                    .orElseThrow(() ->
                            new AccessDeniedException(
                                    "Retry policy does not belong to this project"
                            )
                    );

            queue.setRetryPolicyId(
                    request.getRetryPolicyId()
            );
        }

        Queue savedQueue = queueRepository.save(queue);

        return QueueResponse.from(savedQueue);
    }

    @Transactional(readOnly = true)
    public List<QueueResponse> getQueues(
            Long projectId
    ) {

        getOwnedProject(projectId);

        return queueRepository
                .findByProjectId(projectId)
                .stream()
                .map(QueueResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public QueueResponse getQueue(Long id) {

        return QueueResponse.from(
                getOwnedQueue(id)
        );
    }

    @Transactional
    public QueueResponse pauseQueue(Long id) {

        Queue queue = getOwnedQueue(id);

        queue.setPaused(true);

        return QueueResponse.from(
                queueRepository.save(queue)
        );
    }

    @Transactional
    public QueueResponse resumeQueue(Long id) {

        Queue queue = getOwnedQueue(id);

        queue.setPaused(false);

        return QueueResponse.from(
                queueRepository.save(queue)
        );
    }

    @Transactional(readOnly = true)
    public QueueStatsResponse getStats(Long queueId) {

        getOwnedQueue(queueId);

        long total =
                jobRepository.countByQueueId(queueId);

        long pending =
                jobRepository.countByQueueIdAndStatus(
                        queueId,
                        JobStatus.PENDING
                );

        long claimed =
                jobRepository.countByQueueIdAndStatus(
                        queueId,
                        JobStatus.CLAIMED
                );

        long running =
                jobRepository.countByQueueIdAndStatus(
                        queueId,
                        JobStatus.RUNNING
                );

        long success =
                jobRepository.countByQueueIdAndStatus(
                        queueId,
                        JobStatus.SUCCESS
                );

        long failed =
                jobRepository.countByQueueIdAndStatus(
                        queueId,
                        JobStatus.FAILED
                );

        long retrying =
                jobRepository.countByQueueIdAndStatus(
                        queueId,
                        JobStatus.RETRYING
                );

        long cancelled =
                jobRepository.countByQueueIdAndStatus(
                        queueId,
                        JobStatus.CANCELLED
                );

        return new QueueStatsResponse(
                queueId,
                total,
                pending,
                claimed,
                running,
                success,
                failed,
                retrying,
                cancelled
        );
    }

    @Transactional
    public QueueResponse updateQueue(
            Long id,
            UpdateQueueRequest request
    ) {

        Queue queue = getOwnedQueue(id);

        if (request.getRetryPolicyId() != null) {

            retryPolicyRepository
                    .findByIdAndProjectId(
                            request.getRetryPolicyId(),
                            queue.getProjectId()
                    )
                    .orElseThrow(() ->
                            new AccessDeniedException(
                                    "Retry policy does not belong to this project"
                            )
                    );

            queue.setRetryPolicyId(
                    request.getRetryPolicyId()
            );
        }

        if (request.getPriority() != null) {
            queue.setPriority(
                    request.getPriority()
            );
        }

        if (request.getMaxConcurrency() != null) {
            queue.setMaxConcurrency(
                    request.getMaxConcurrency()
            );
        }

        return QueueResponse.from(
                queueRepository.save(queue)
        );
    }
}