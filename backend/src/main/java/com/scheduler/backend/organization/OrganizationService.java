package com.scheduler.backend.organization;

import com.scheduler.backend.organization.dto.CreateOrganizationRequest;
import com.scheduler.backend.organization.dto.OrganizationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(
            OrganizationRepository organizationRepository
    ) {
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    public OrganizationResponse createOrganization(
            CreateOrganizationRequest request
    ) {

        if (organizationRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException(
                    "Organization already exists"
            );
        }

        Organization organization = new Organization();

        organization.setName(request.getName());

        Organization savedOrganization =
                organizationRepository.save(organization);

        return OrganizationResponse.from(savedOrganization);
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponse> getOrganizations() {

        return organizationRepository
                .findAll()
                .stream()
                .map(OrganizationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrganizationResponse getOrganization(Long id) {

        Organization organization =
                organizationRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Organization not found"
                                )
                        );

        return OrganizationResponse.from(organization);
    }
}