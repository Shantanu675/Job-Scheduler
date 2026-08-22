package com.scheduler.backend.organization.dto;

import com.scheduler.backend.organization.Organization;

import java.time.Instant;

public record OrganizationResponse(
        Long id,
        String name,
        Instant createdAt,
        Instant updatedAt
) {

    public static OrganizationResponse from(
            Organization organization
    ) {
        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getCreatedAt(),
                organization.getUpdatedAt()
        );
    }
}
