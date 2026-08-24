package com.scheduler.backend.project;

import com.scheduler.backend.auth.User;
import com.scheduler.backend.auth.UserRepository;
import com.scheduler.backend.config.SecurityUtils;
import com.scheduler.backend.project.dto.CreateProjectRequest;
import com.scheduler.backend.project.dto.ProjectResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            UserRepository userRepository
    ) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
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

    @Transactional
    public ProjectResponse createProject(
            CreateProjectRequest request
    ) {

        User user = getCurrentUser();

        Long organizationId = user.getOrganizationId();

        if (organizationId == null) {
            throw new IllegalStateException(
                    "User is not assigned to an organization"
            );
        }

        if (projectRepository.existsByOrganizationIdAndName(
                organizationId,
                request.getName()
        )) {
            throw new IllegalArgumentException(
                    "Project already exists for this organization"
            );
        }

        Project project = new Project();

        project.setOrganizationId(organizationId);
        project.setName(request.getName());
        project.setDescription(request.getDescription());

        return ProjectResponse.from(
                projectRepository.save(project)
        );
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjects(
            Long organizationId
    ) {

        User user = getCurrentUser();

        if (user.getOrganizationId() == null ||
                !user.getOrganizationId().equals(organizationId)) {

            throw new AccessDeniedException(
                    "You do not have access to this organization"
            );
        }

        return projectRepository
                .findByOrganizationId(organizationId)
                .stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long id) {

        User user = getCurrentUser();

        Long organizationId = user.getOrganizationId();

        if (organizationId == null) {
            throw new AccessDeniedException(
                    "User is not assigned to an organization"
            );
        }

        Project project = projectRepository
                .findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "You do not have access to this project"
                        )
                );

        return ProjectResponse.from(project);
    }
}