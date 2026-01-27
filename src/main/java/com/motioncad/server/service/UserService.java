package com.motioncad.server.service;

import com.motioncad.server.domain.User;
import com.motioncad.server.domain.UserSettings;
import com.motioncad.server.dto.UserUpdateRequestDTO;
import com.motioncad.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    @Transactional
    public void updateUserProfile(Long userId, UserUpdateRequestDTO updateDto) {
        User user = getUser(userId);

        if (updateDto.nickname() != null) {
            user.setNickname(updateDto.nickname());
        }
        if (updateDto.region() != null) {
            user.setRegion(updateDto.region());
        }
        if (updateDto.job() != null) {
            user.setJob(updateDto.job());
        }
        if (updateDto.userDescription() != null) {
            user.setUserDescription(updateDto.userDescription());
        }
    }

    @Transactional
    public void updateUserSettings(Long userId, UserSettings settings) {
        User user = getUser(userId);
        user.setUserSettings(settings);
    }
}
