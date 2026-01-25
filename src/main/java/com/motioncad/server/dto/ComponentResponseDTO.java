package com.motioncad.server.dto;

import com.motioncad.server.domain.ProjectComponent;

public record ComponentResponseDTO(
        Long id,
        Long partId,
        String partName,
        Double posX,
        Double posY,
        Double posZ,
        Double rotX,
        Double rotY,
        Double rotZ,
        Double scaleX,
        Double scaleY,
        Double scaleZ) {
    public static ComponentResponseDTO from(ProjectComponent component) {
        return new ComponentResponseDTO(
                component.getId(),
                component.getPart().getId(),
                component.getPart().getName(),
                component.getPosX(),
                component.getPosY(),
                component.getPosZ(),
                component.getRotX(),
                component.getRotY(),
                component.getRotZ(),
                component.getScaleX(),
                component.getScaleY(),
                component.getScaleZ());
    }
}
