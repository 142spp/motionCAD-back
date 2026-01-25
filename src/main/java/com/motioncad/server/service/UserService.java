package com.motioncad.server.service;

import com.motioncad.server.domain.User;
import com.motioncad.server.domain.UserSettings;
import com.motioncad.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public Long signUp(String email, String passwordHash, String nickname) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists: " + email);
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordHash)
                .nickname(nickname)
                .userSettings(UserSettings.builder()
                        .handSensitivity(50)
                        .isLeftHanded(false)
                        .cameraResolution("720p")
                        .uiTheme("dark")
                        .build())
                .build();

        return userRepository.save(user).getId();
    }

    @Transactional(readOnly = true)
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    @Transactional
    public void updateUserSettings(Long userId, UserSettings settings) {
        User user = getUser(userId);
        user.setUserSettings(settings);
    }
}
