package com.scheduler.backend.project;

import com.scheduler.backend.project.dto.CreateProjectRequest;
import com.scheduler.backend.project.dto.ProjectResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(
            @Valid @RequestBody CreateProjectRequest request
    ) {
        return projectService.createProject(request);
    }

    @GetMapping
    public List<ProjectResponse> getProjects(
            @RequestParam Long organizationId
    ) {
        return projectService.getProjects(organizationId);
    }

    @GetMapping("/{id}")
    public ProjectResponse getProject(
            @PathVariable Long id
    ) {
        return projectService.getProject(id);
    }
}