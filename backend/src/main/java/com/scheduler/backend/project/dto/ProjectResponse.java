package com.scheduler.backend.project.dto;

import com.scheduler.backend.project.Project;

import java.time.Instant;

public record ProjectResponse(
        Long id,
        Long organizationId,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getOrganizationId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
