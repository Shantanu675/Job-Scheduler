package com.scheduler.backend.dlq;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "dead_letter_queue",
        indexes = {
                @Index(
                        name = "idx_dlq_job",
                        columnList = "job_id"
                ),
                @Index(
                        name = "idx_dlq_moved_at",
                        columnList = "moved_at"
                )
        }
)
public class DeadLetterJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(length = 255)
    private String reason;

    @Column(name = "final_error", columnDefinition = "TEXT")
    private String finalError;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "moved_at", nullable = false)
    private Instant movedAt;

    @PrePersist
    protected void onCreate() {
        if (movedAt == null) {
            movedAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getFinalError() {
        return finalError;
    }

    public void setFinalError(String finalError) {
        this.finalError = finalError;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Instant getMovedAt() {
        return movedAt;
    }
}
