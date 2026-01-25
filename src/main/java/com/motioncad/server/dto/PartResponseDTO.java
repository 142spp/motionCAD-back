package com.motioncad.server.dto;

import com.motioncad.server.domain.Part;
import com.motioncad.server.domain.PartType;

import java.time.LocalDateTime;

public record PartResponseDTO(
        Long id,
        String name,
        PartType type,
        String description,
        String category,
        String thumbnailUrl,
        String modelFileUrl,
        boolean isPublic,
        String creatorNickname,
        int likesCount,
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
                part.getCreator() != null ? part.getCreator().getNickname() : "System",
                part.getLikesCount(),
                part.getCreatedAt(),
                part.getUpdatedAt());
    }
}
