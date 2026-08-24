package com.scheduler.backend.retrypolicy.dto;

import com.scheduler.backend.retrypolicy.RetryPolicy;

import java.time.Instant;

public record RetryPolicyResponse(
        Long id,
        Long projectId,
        String name,
        Integer maxRetries,
        String backoffType,
        Long initialDelayMs,
        Long maxDelayMs,
        Instant createdAt
) {

    public static RetryPolicyResponse from(RetryPolicy policy) {
        return new RetryPolicyResponse(
                policy.getId(),
                policy.getProjectId(),
                policy.getName(),
                policy.getMaxRetries(),
                policy.getBackoffType(),
                policy.getInitialDelayMs(),
                policy.getMaxDelayMs(),
                policy.getCreatedAt()
        );
    }
}
