package com.motioncad.server.dto;

import com.motioncad.server.domain.Part;
import com.motioncad.server.domain.PartCategory;
import com.motioncad.server.domain.PartType;

import java.time.LocalDateTime;

public record PartResponseDTO(
        Long id,
        String name,
        PartType type,
        String description,
        PartCategory category,
        String thumbnailUrl,
        String modelFileUrl,
        boolean isPublic,
        boolean isAiGenerated,
        String creatorNickname,
        int likesCount,
        int viewsCount,
        int commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static PartResponseDTO from(Part part) {
        return new PartResponseDTO(
                part.getId(),
                part.getName(),
                part.getType(),
                part.getDescription(),
                part.getCategory(),
                part.getThumbnailUrl(),
                part.getModelFileUrl(),
                part.isPublic(),
                part.isAiGenerated(),
                part.getCreator() != null ? part.getCreator().getNickname() : "System",
                part.getLikesCount(),
                part.getViewsCount(),
                part.getCommentCount(),
                part.getCreatedAt(),
                part.getUpdatedAt());
    }
}
