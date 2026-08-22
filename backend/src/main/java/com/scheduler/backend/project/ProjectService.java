package com.scheduler.backend.project;

import com.scheduler.backend.project.dto.CreateProjectRequest;
import com.scheduler.backend.project.dto.ProjectResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {

        if (projectRepository.existsByOrganizationIdAndName(
                request.getOrganizationId(),
                request.getName()
        )) {
            throw new IllegalArgumentException(
                    "Project already exists for this organization"
            );
        }

        Project project = new Project();

        project.setOrganizationId(request.getOrganizationId());
        project.setName(request.getName());
        project.setDescription(request.getDescription());

        Project savedProject = projectRepository.save(project);

        return ProjectResponse.from(savedProject);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjects(Long organizationId) {

        return projectRepository
                .findByOrganizationId(organizationId)
                .stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long id) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Project not found")
                );

        return ProjectResponse.from(project);
    }
}