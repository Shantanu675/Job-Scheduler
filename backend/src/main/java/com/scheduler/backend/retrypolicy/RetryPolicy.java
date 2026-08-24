package com.scheduler.backend.retrypolicy;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "retry_policies",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_retry_policy_project_name",
                        columnNames = {"project_id", "name"}
                )
        }
)
public class RetryPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries = 3;

    @Column(name = "backoff_type", nullable = false, length = 30)
    private String backoffType = "EXPONENTIAL";

    @Column(name = "initial_delay_ms", nullable = false)
    private Long initialDelayMs = 1000L;

    @Column(name = "max_delay_ms", nullable = false)
    private Long maxDelayMs = 60000L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

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

    public Instant getCreatedAt() {
        return createdAt;
    }
}