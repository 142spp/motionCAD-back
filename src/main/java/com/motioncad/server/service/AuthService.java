package com.motioncad.server.service;

import com.motioncad.server.domain.User;
import com.motioncad.server.domain.UserSettings;
import com.motioncad.server.dto.AuthDTOs.*;
import com.motioncad.server.repository.UserRepository;
import com.motioncad.server.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public void signUp(SignupRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("Email already exists: " + dto.email());
        }

        User user = User.builder()
                .email(dto.email())
                .passwordHash(passwordEncoder.encode(dto.password()))
                .nickname(dto.nickname())
                .provider("LOCAL")
                .userSettings(UserSettings.builder()
                        .handSensitivity(50)
                        .isLeftHanded(false)
                        .cameraResolution("720p")
                        .uiTheme("dark")
                        .build())
                .build();

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public TokenResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        String token = tokenProvider.createToken(user.getEmail());
        return new TokenResponseDTO(token);
    }
}
