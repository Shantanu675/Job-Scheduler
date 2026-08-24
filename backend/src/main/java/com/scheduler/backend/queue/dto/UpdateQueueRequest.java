package com.scheduler.backend.queue.dto;

import jakarta.validation.constraints.Min;

public class UpdateQueueRequest {

    private Long retryPolicyId;

    private Integer priority;

    @Min(1)
    private Integer maxConcurrency;

    public Long getRetryPolicyId() {
        return retryPolicyId;
    }

    public void setRetryPolicyId(Long retryPolicyId) {
        this.retryPolicyId = retryPolicyId;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getMaxConcurrency() {
        return maxConcurrency;
    }

    public void setMaxConcurrency(Integer maxConcurrency) {
        this.maxConcurrency = maxConcurrency;
    }
}
