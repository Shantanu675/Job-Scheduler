package com.scheduler.backend.job.dto;

import com.scheduler.backend.job.Job;
import com.scheduler.backend.job.JobStatus;

import java.time.Instant;

public record JobResponse(
        Long id,
        Long projectId,
        Long queueId,
        Long retryPolicyId,
        Long workerId,
        String jobType,
        String payload,
        JobStatus status,
        Integer priority,
        Integer retryCount,
        Integer maxRetries,
        Instant availableAt,
        Instant claimedAt,
        Instant startedAt,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static JobResponse from(Job job) {
        return new JobResponse(
                job.getId(),
                job.getProjectId(),
                job.getQueueId(),
                job.getRetryPolicyId(),
                job.getWorkerId(),
                job.getJobType(),
                job.getPayload(),
                job.getStatus(),
                job.getPriority(),
                job.getRetryCount(),
                job.getMaxRetries(),
                job.getAvailableAt(),
                job.getClaimedAt(),
                job.getStartedAt(),
                job.getCompletedAt(),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }
}
