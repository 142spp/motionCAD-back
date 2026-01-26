package com.motioncad.server.dto;

import com.motioncad.server.domain.Part;
import com.motioncad.server.domain.PartCategory;
import com.motioncad.server.domain.PartType;
import com.motioncad.server.service.S3Service;

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
        return from(part, null);
    }

    public static PartResponseDTO from(Part part, S3Service s3Service) {
        String thumbUrl = part.getThumbnailUrl();
        String modelUrl = part.getModelFileUrl();

        if (s3Service != null) {
            thumbUrl = s3Service.generatePresignedUrl(thumbUrl);
            modelUrl = s3Service.generatePresignedUrl(modelUrl);
        }

        return new PartResponseDTO(
                part.getId(),
                part.getName(),
                part.getType(),
                part.getDescription(),
                part.getCategory(),
                thumbUrl,
                modelUrl,
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
