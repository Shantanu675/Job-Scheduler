package com.scheduler.backend.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByOrganizationId(Long organizationId);

    Optional<Project> findByOrganizationIdAndName(
            Long organizationId,
            String name
    );

    boolean existsByOrganizationIdAndName(
            Long organizationId,
            String name
    );
}