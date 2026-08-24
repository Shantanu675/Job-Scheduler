package com.scheduler.backend.dlq;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeadLetterJobRepository
        extends JpaRepository<DeadLetterJob, Long> {

    List<DeadLetterJob> findByJobId(Long jobId);
}