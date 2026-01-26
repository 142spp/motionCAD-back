package com.motioncad.server.controller;

import com.motioncad.server.dto.ProjectRequestDTO;
import com.motioncad.server.dto.ProjectResponseDTO;
import com.motioncad.server.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project", description = "Project management APIs")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "List Public Projects", description = "Fetches a list of public projects sorted by latest, likes, views, or comments, and filtered by time range.")
    public List<ProjectResponseDTO> getPublicProjects(
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) String timeRange) {
        return projectService.getPublicProjects(sort, timeRange);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Save or Update Project", description = "Creates a new project or updates an existing one with its components.")
    public Long saveProject(@RequestParam Long userId, @RequestParam(required = false) Long projectId,
            @RequestBody @Valid ProjectRequestDTO dto) {
        return projectService.saveProject(userId, projectId, dto);
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "Get Project Detail", description = "Fetches a project's metadata and all its components.")
    public ProjectResponseDTO getProjectDetail(@PathVariable Long projectId) {
        return projectService.getProjectDetail(projectId);
    }

    @PostMapping("/{projectId}/like")
    @Operation(summary = "Like Project", description = "Increases the like count of a project.")
    public void likeProject(@PathVariable Long projectId) {
        projectService.addLike(projectId);
    }

    @PostMapping("/{projectId}/comment")
    @Operation(summary = "Increment Comment Count", description = "Increases the comment count of a project.")
    public void commentProject(@PathVariable Long projectId) {
        projectService.addComment(projectId);
    }
}
