package com.motioncad.server.dto;

import jakarta.validation.constraints.NotNull;

public record ComponentRequestDTO(
        @NotNull Long partId,
        Double posX,
        Double posY,
        Double posZ,
        Double rotX,
        Double rotY,
        Double rotZ,
        Double scaleX,
        Double scaleY,
        Double scaleZ) {

    public com.motioncad.server.domain.ProjectComponent toEntity(com.motioncad.server.domain.Project project,
            com.motioncad.server.domain.Part part) {
        return com.motioncad.server.domain.ProjectComponent.builder()
                .project(project)
                .part(part)
                .posX(posX)
                .posY(posY)
                .posZ(posZ)
                .rotX(rotX)
                .rotY(rotY)
                .rotZ(rotZ)
                .scaleX(scaleX)
                .scaleY(scaleY)
                .scaleZ(scaleZ)
                .build();
    }
}
