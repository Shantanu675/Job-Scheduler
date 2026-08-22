package com.scheduler.backend.organization;

import com.scheduler.backend.organization.dto.CreateOrganizationRequest;
import com.scheduler.backend.organization.dto.OrganizationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(
            OrganizationService organizationService
    ) {
        this.organizationService = organizationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationResponse createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request
    ) {
        return organizationService.createOrganization(request);
    }

    @GetMapping
    public List<OrganizationResponse> getOrganizations() {
        return organizationService.getOrganizations();
    }

    @GetMapping("/{id}")
    public OrganizationResponse getOrganization(
            @PathVariable Long id
    ) {
        return organizationService.getOrganization(id);
    }
}