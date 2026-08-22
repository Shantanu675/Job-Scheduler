package com.scheduler.backend.jobexecution;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobExecutionRepository
        extends JpaRepository<JobExecution, Long> {

    List<JobExecution> findByJobId(Long jobId);

    List<JobExecution> findByJobIdOrderByAttemptNumberAsc(Long jobId);
}