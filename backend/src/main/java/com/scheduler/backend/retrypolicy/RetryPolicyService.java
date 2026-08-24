package com.scheduler.backend.retrypolicy;

import com.scheduler.backend.auth.User;
import com.scheduler.backend.auth.UserRepository;
import com.scheduler.backend.config.SecurityUtils;
import com.scheduler.backend.project.ProjectRepository;
import com.scheduler.backend.retrypolicy.dto.CreateRetryPolicyRequest;
import com.scheduler.backend.retrypolicy.dto.RetryPolicyResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RetryPolicyService {

    private final RetryPolicyRepository retryPolicyRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public RetryPolicyService(
            RetryPolicyRepository retryPolicyRepository,
            UserRepository userRepository,
            ProjectRepository projectRepository
    ) {
        this.retryPolicyRepository = retryPolicyRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    private User getCurrentUser() {

        Long userId = SecurityUtils.getCurrentUserId();

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user not found"
                        )
                );
    }

    private void verifyProjectOwnership(Long projectId) {

        User user = getCurrentUser();

        Long organizationId = user.getOrganizationId();

        if (organizationId == null) {
            throw new AccessDeniedException(
                    "User is not assigned to an organization"
            );
        }

        projectRepository
                .findByIdAndOrganizationId(
                        projectId,
                        organizationId
                )
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "You do not have access to this project"
                        )
                );
    }

    @Transactional
    public RetryPolicyResponse createPolicy(
            CreateRetryPolicyRequest request
    ) {

        verifyProjectOwnership(request.getProjectId());

        if (retryPolicyRepository.existsByProjectIdAndName(
                request.getProjectId(),
                request.getName()
        )) {
            throw new IllegalArgumentException(
                    "Retry policy already exists for this project"
            );
        }

        if (request.getInitialDelayMs() >
                request.getMaxDelayMs()) {

            throw new IllegalArgumentException(
                    "Initial delay cannot exceed maximum delay"
            );
        }

        RetryPolicy policy = new RetryPolicy();

        policy.setProjectId(request.getProjectId());
        policy.setName(request.getName());
        policy.setMaxRetries(request.getMaxRetries());
        policy.setBackoffType(
                request.getBackoffType().toUpperCase()
        );
        policy.setInitialDelayMs(
                request.getInitialDelayMs()
        );
        policy.setMaxDelayMs(
                request.getMaxDelayMs()
        );

        return RetryPolicyResponse.from(
                retryPolicyRepository.save(policy)
        );
    }

    @Transactional(readOnly = true)
    public List<RetryPolicyResponse> getPolicies(
            Long projectId
    ) {

        verifyProjectOwnership(projectId);

        return retryPolicyRepository
                .findByProjectId(projectId)
                .stream()
                .map(RetryPolicyResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RetryPolicyResponse getPolicy(Long id) {

        RetryPolicy policy =
                retryPolicyRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Retry policy not found: " + id
                                )
                        );

        verifyProjectOwnership(policy.getProjectId());

        return RetryPolicyResponse.from(policy);
    }
}