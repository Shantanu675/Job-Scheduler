package com.scheduler.backend.dlq.dto;

import com.scheduler.backend.dlq.DeadLetterJob;

import java.time.Instant;

public record DeadLetterJobResponse(
        Long id,
        Long jobId,
        String reason,
        String finalError,
        Integer retryCount,
        Instant movedAt
) {

    public static DeadLetterJobResponse from(DeadLetterJob dlqJob) {
        return new DeadLetterJobResponse(
                dlqJob.getId(),
                dlqJob.getJobId(),
                dlqJob.getReason(),
                dlqJob.getFinalError(),
                dlqJob.getRetryCount(),
                dlqJob.getMovedAt()
        );
    }
}
