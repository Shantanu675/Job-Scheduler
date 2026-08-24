package com.scheduler.backend.retrypolicy;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RetryPolicyRepository
        extends JpaRepository<RetryPolicy, Long> {

    List<RetryPolicy> findByProjectId(Long projectId);

    Optional<RetryPolicy> findByProjectIdAndName(
            Long projectId,
            String name
    );

    Optional<RetryPolicy> findByIdAndProjectId(
            Long id,
            Long projectId
    );

    boolean existsByProjectIdAndName(
            Long projectId,
            String name
    );
}
