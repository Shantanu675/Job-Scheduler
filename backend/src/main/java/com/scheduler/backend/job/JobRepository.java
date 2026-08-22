package com.scheduler.backend.job;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByQueueId(Long queueId);

    List<Job> findByQueueIdAndStatus(
            Long queueId,
            JobStatus status
    );

    List<Job> findByWorkerIdAndStatusIn(
            Long workerId,
            List<JobStatus> statuses
    );

    @Query("""
            SELECT j
            FROM Job j
            WHERE j.queueId = :queueId
              AND j.status = :status
              AND j.availableAt <= :now
            ORDER BY j.priority DESC, j.id ASC
            """)
    List<Job> findAvailableJobs(
            @Param("queueId") Long queueId,
            @Param("status") JobStatus status,
            @Param("now") Instant now
    );

    @Query("""
        SELECT COUNT(j)
        FROM Job j
        WHERE j.queueId = :queueId
          AND j.status IN :statuses
        """)
    long countActiveJobs(
            @Param("queueId") Long queueId,
            @Param("statuses") List<JobStatus> statuses
    );

    @Modifying
    @Query("""
            UPDATE Job j
            SET j.status = :claimedStatus,
                j.workerId = :workerId,
                j.claimedAt = :claimedAt
            WHERE j.id = :jobId
              AND j.status = :pendingStatus
              AND j.availableAt <= :claimedAt
            """)
    int claimJob(
            @Param("jobId") Long jobId,
            @Param("workerId") Long workerId,
            @Param("pendingStatus") JobStatus pendingStatus,
            @Param("claimedAt") Instant claimedAt,
            @Param("claimedStatus") JobStatus claimedStatus
    );

    @Modifying
    @Query("""
        UPDATE Job j
        SET j.status = com.scheduler.backend.job.JobStatus.PENDING,
            j.workerId = NULL,
            j.claimedAt = NULL,
            j.startedAt = NULL
        WHERE j.status IN (
            com.scheduler.backend.job.JobStatus.CLAIMED,
            com.scheduler.backend.job.JobStatus.RUNNING
        )
        AND j.claimedAt < :threshold
    """)
    int recoverStaleJobs(@Param("threshold") Instant threshold);
}