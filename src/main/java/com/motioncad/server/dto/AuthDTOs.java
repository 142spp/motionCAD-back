package com.motioncad.server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDTOs {

    public record LoginRequestDTO(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record SignupRequestDTO(
            @NotBlank @Email String email,
            @NotBlank String password,
            @NotBlank String nickname) {
    }

    public record TokenResponseDTO(
            String accessToken,
            String tokenType) {
        public TokenResponseDTO(String accessToken) {
            this(accessToken, "Bearer");
        }
    }
}
