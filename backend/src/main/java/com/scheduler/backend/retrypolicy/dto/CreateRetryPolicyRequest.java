package com.scheduler.backend.retrypolicy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateRetryPolicyRequest {

    @NotNull
    private Long projectId;

    @NotBlank
    @Size(max = 100)
    private String name;

    @Min(0)
    private Integer maxRetries = 3;

    @NotBlank
    private String backoffType = "EXPONENTIAL";

    @Min(0)
    private Long initialDelayMs = 1000L;

    @Min(0)
    private Long maxDelayMs = 60000L;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public String getBackoffType() {
        return backoffType;
    }

    public void setBackoffType(String backoffType) {
        this.backoffType = backoffType;
    }

    public Long getInitialDelayMs() {
        return initialDelayMs;
    }

    public void setInitialDelayMs(Long initialDelayMs) {
        this.initialDelayMs = initialDelayMs;
    }

    public Long getMaxDelayMs() {
        return maxDelayMs;
    }

    public void setMaxDelayMs(Long maxDelayMs) {
        this.maxDelayMs = maxDelayMs;
    }
}
