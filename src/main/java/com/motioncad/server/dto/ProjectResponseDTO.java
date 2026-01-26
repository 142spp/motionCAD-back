package com.motioncad.server.dto;

import com.motioncad.server.domain.Project;
import com.motioncad.server.service.S3Service;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectResponseDTO(
		Long id,
		String title,
		String description,
		String nickname,
		Long backgroundPartId,
		String backgroundPartName,
		boolean isPublic,
		String previewImageUrl,
		int likesCount,
		int viewsCount,
		int commentCount,
		List<ComponentResponseDTO> components,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
	public static ProjectResponseDTO from(Project project) {
		return from(project, null);
	}

	public static ProjectResponseDTO from(Project project, S3Service s3Service) {
		String previewUrl = project.getPreviewImageUrl();
		if (s3Service != null) {
			previewUrl = s3Service.generatePresignedUrl(previewUrl);
		}

		return new ProjectResponseDTO(
				project.getId(),
				project.getTitle(),
				project.getDescription(),
				project.getUser().getNickname(),
				project.getBackgroundPart() != null ? project.getBackgroundPart().getId() : null,
				project.getBackgroundPart() != null ? project.getBackgroundPart().getName() : null,
				project.isPublic(),
				previewUrl,
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
