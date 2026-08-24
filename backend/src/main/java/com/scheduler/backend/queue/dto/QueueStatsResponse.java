package com.scheduler.backend.queue.dto;

public record QueueStatsResponse(
        Long queueId,
        long total,
        long pending,
        long claimed,
        long running,
        long success,
        long failed,
        long retrying,
        long cancelled
) {
}
