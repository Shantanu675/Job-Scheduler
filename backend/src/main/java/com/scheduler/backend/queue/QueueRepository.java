package com.scheduler.backend.queue;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QueueRepository extends JpaRepository<Queue, Long> {

    List<Queue> findByProjectId(Long projectId);

    Optional<Queue> findByIdAndProjectId(
            Long id,
            Long projectId
    );

    Optional<Queue> findByProjectIdAndName(
            Long projectId,
            String name
    );

    boolean existsByProjectIdAndName(
            Long projectId,
            String name
    );
}