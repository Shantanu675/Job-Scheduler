package com.scheduler.backend.worker.dto;

import jakarta.validation.constraints.NotBlank;

public class RegisterWorkerRequest {

    @NotBlank
    private String workerId;

    private String hostname;

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}
