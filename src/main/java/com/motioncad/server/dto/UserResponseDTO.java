package com.motioncad.server.dto;

import com.motioncad.server.domain.User;
import com.motioncad.server.domain.UserSettings;

import java.time.LocalDateTime;

public record UserResponseDTO(
        Long id,
        String email,
        String nickname,
        UserSettings userSettings,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getUserSettings(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
