package com.motioncad.server.dto;

import jakarta.validation.constraints.Size;

public record UserUpdateRequestDTO(
        @Size(min = 2, max = 20) String nickname,

        String region,

        String job,

        String userDescription) {
}
