package com.scheduler.backend.worker.dto;

import java.time.Instant;

public class WorkerResponse {

    private Long id;
    private String workerId;
    private String hostname;
    private String status;
    private Instant lastHeartbeatAt;
    private Instant createdAt;
    private Instant updatedAt;

    public WorkerResponse(
            Long id,
            String workerId,
            String hostname,
            String status,
            Instant lastHeartbeatAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.workerId = workerId;
        this.hostname = hostname;
        this.status = status;
        this.lastHeartbeatAt = lastHeartbeatAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getWorkerId() {
        return workerId;
    }

    public String getHostname() {
        return hostname;
    }

    public String getStatus() {
        return status;
    }

    public Instant getLastHeartbeatAt() {
        return lastHeartbeatAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
