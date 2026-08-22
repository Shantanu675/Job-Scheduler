package com.scheduler.backend.queue.dto;

import com.scheduler.backend.queue.Queue;

import java.time.Instant;

public record QueueResponse(
        Long id,
        Long projectId,
        String name,
        Integer priority,
        Integer maxConcurrency,
        Instant createdAt,
        Instant updatedAt
) {

    public static QueueResponse from(Queue queue) {
        return new QueueResponse(
                queue.getId(),
                queue.getProjectId(),
                queue.getName(),
                queue.getPriority(),
                queue.getMaxConcurrency(),
                queue.getCreatedAt(),
                queue.getUpdatedAt()
        );
    }
}