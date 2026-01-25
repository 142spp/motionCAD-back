package com.motioncad.server.dto;

import com.motioncad.server.domain.Project;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectResponseDTO(
        Long id,
        String title,
        String description,
        String nickname,
        boolean isPublic,
        String previewImageUrl,
        int likesCount,
        int viewsCount,
        int commentCount,
        List<ComponentResponseDTO> components,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static ProjectResponseDTO from(Project project) {
        return new ProjectResponseDTO(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getUser().getNickname(),
                project.isPublic(),
                project.getPreviewImageUrl(),
                project.getLikesCount(),
                project.getViewsCount(),
                project.getCommentCount(),
                project.getComponents().stream()
                        .map(ComponentResponseDTO::from)
                        .toList(),
                project.getCreatedAt(),
                project.getUpdatedAt());
    }
}
