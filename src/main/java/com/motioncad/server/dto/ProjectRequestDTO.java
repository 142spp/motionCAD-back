package com.motioncad.server.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ProjectRequestDTO(
        @NotBlank String title,
        String description,
        Long backgroundPartId,
        boolean isPublic,
        String previewImageUrl,
        List<ComponentRequestDTO> components) {

    public com.motioncad.server.domain.Project toEntity(com.motioncad.server.domain.User user) {
        return com.motioncad.server.domain.Project.builder()
                .user(user)
                .title(title)
                .description(description)
                .isPublic(isPublic)
                .previewImageUrl(previewImageUrl)
                .build();
    }
}
