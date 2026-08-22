package com.scheduler.backend.worker;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface WorkerRepository extends JpaRepository<Worker, Long> {

    Optional<Worker> findByWorkerId(String workerId);

    @Modifying
    @Query("""
        UPDATE Worker w
        SET w.status = 'OFFLINE'
        WHERE w.status = 'ONLINE'
          AND w.lastHeartbeatAt < :threshold
    """)
    int markOfflineWorkers(@Param("threshold") Instant threshold);
}
