package com.motioncad.server.service;

import com.motioncad.server.domain.Part;
import com.motioncad.server.domain.Project;
import com.motioncad.server.domain.ProjectComponent;
import com.motioncad.server.domain.User;
import com.motioncad.server.dto.ProjectRequestDTO;
import com.motioncad.server.dto.ProjectResponseDTO;
import com.motioncad.server.repository.PartRepository;
import com.motioncad.server.repository.ProjectComponentRepository;
import com.motioncad.server.repository.ProjectRepository;
import com.motioncad.server.repository.ProjectSpecification;
import com.motioncad.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectComponentRepository componentRepository;
    private final UserRepository userRepository;
    private final PartRepository partRepository;

    @Transactional
    public Long saveProject(Long userId, Long projectId, ProjectRequestDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Project project;
        if (projectId != null) {
            project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));

            // Check ownership (simple check for now)
            if (!project.getUser().getId().equals(userId)) {
                throw new RuntimeException("No permission to modify this project");
            }

            project.setTitle(dto.title());
            project.setDescription(dto.description());
            project.setPublic(dto.isPublic());
            project.setPreviewImageUrl(dto.previewImageUrl());

            // Handle background
            if (dto.backgroundPartId() != null) {
                Part background = partRepository.findById(dto.backgroundPartId())
                        .orElseThrow(
                                () -> new RuntimeException("Background part not found: " + dto.backgroundPartId()));
                project.setBackgroundPart(background);
            }

            // Bulk Delete existing components for update
            componentRepository.deleteAllByProjectId(projectId);
        } else {
            project = dto.toEntity(user);
            if (dto.backgroundPartId() != null) {
                Part background = partRepository.findById(dto.backgroundPartId())
                        .orElseThrow(
                                () -> new RuntimeException("Background part not found: " + dto.backgroundPartId()));
                project.setBackgroundPart(background);
            }
            projectRepository.save(project);
        }

        // Save New Components
        if (dto.components() != null) {
            Project finalProject = project;
            List<ProjectComponent> components = dto.components().stream()
                    .map(compDto -> {
                        Part part = partRepository.findById(compDto.partId())
                                .orElseThrow(() -> new RuntimeException("Part not found: " + compDto.partId()));
                        return compDto.toEntity(finalProject, part);
                    }).toList();

            componentRepository.saveAll(components);
        }

        return project.getId();
    }

    @Transactional(readOnly = true)
    public ProjectResponseDTO getProjectDetail(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));

        return ProjectResponseDTO.from(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> getPublicProjects(String sortBy, String timeRange) {
        Specification<Project> spec = Specification.where(ProjectSpecification.isPublic());

        if (timeRange != null) {
            LocalDateTime start = calculateStartTime(timeRange);
            spec = spec.and(ProjectSpecification.updatedAfter(start));
        }

        Sort sort = calculateSort(sortBy);
        return projectRepository.findAll(spec, sort).stream()
                .map(ProjectResponseDTO::from)
                .toList();
    }

    private Sort calculateSort(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "likes" -> Sort.by(Sort.Direction.DESC, "likesCount");
            case "views" -> Sort.by(Sort.Direction.DESC, "viewsCount");
            case "comments" -> Sort.by(Sort.Direction.DESC, "commentCount");
            default -> Sort.by(Sort.Direction.DESC, "updatedAt");
        };
    }

    private LocalDateTime calculateStartTime(String timeRange) {
        LocalDateTime now = LocalDateTime.now();
        return switch (timeRange.toLowerCase()) {
            case "day" -> now.minusDays(1);
            case "week" -> now.minusWeeks(1);
            case "month" -> now.minusMonths(1);
            case "year" -> now.minusYears(1);
            default -> null;
        };
    }

    @Transactional
    public void addLike(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));
        project.setLikesCount(project.getLikesCount() + 1);
    }

    @Transactional
    public void addComment(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));
        project.setCommentCount(project.getCommentCount() + 1);
    }
}
