package com.motioncad.server.dto;

import com.motioncad.server.domain.User;
import com.motioncad.server.domain.UserSettings;

import java.time.LocalDateTime;

public record UserResponseDTO(
        Long id,
        String email,
        String nickname,
        UserSettings userSettings,
        Integer totalProjects,
        Integer totalLikes,
        Integer totalViews,
        String region,
        String job,
        String userDescription,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getUserSettings(),
                user.getTotalProjects(),
                user.getTotalLikes(),
                user.getTotalViews(),
                user.getRegion(),
                user.getJob(),
                user.getUserDescription(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
