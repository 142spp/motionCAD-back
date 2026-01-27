package com.motioncad.server.controller;

import com.motioncad.server.domain.User;
import com.motioncad.server.domain.UserSettings;
import com.motioncad.server.dto.UserResponseDTO;
import com.motioncad.server.dto.UserUpdateRequestDTO;
import com.motioncad.server.service.UserService;
import com.motioncad.server.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User management APIs")
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get User Profile", description = "Fetches basic profile information including nickname and settings.")
    public UserResponseDTO getUserProfile(@PathVariable Long userId) {
        User user = userService.getUser(userId);
        return UserResponseDTO.from(user);
    }

    @PatchMapping("/{userId}/settings")
    @Operation(summary = "Update User Settings", description = "Updates JSON-based user settings (hand sensitivity, camera, etc.)")
    public void updateUserSettings(@PathVariable Long userId, @RequestBody UserSettings settings,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (!userId.equals(userPrincipal.getId())) {
            throw new RuntimeException("Unauthorized to update other user's settings");
        }
        userService.updateUserSettings(userId, settings);
    }

    @PatchMapping("/{userId}")
    @Operation(summary = "Update User Profile", description = "Updates user profile information (nickname, region, job, description)")
    public void updateProfile(@PathVariable Long userId, @RequestBody UserUpdateRequestDTO updateDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (!userId.equals(userPrincipal.getId())) {
            throw new RuntimeException("Unauthorized to update other user's profile");
        }
        userService.updateUserProfile(userId, updateDto);
    }

    @GetMapping("/me")
    @Operation(summary = "Get Current User Profile", description = "Fetches the profile of the currently authenticated user.")
    public UserResponseDTO getCurrentUserProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userService.getUser(userPrincipal.getId());
        return UserResponseDTO.from(user);
    }

    @PatchMapping("/me")
    @Operation(summary = "Update Current User Profile", description = "Updates the profile of the currently authenticated user.")
    public void updateCurrentUserProfile(@AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody UserUpdateRequestDTO updateDto) {
        userService.updateUserProfile(userPrincipal.getId(), updateDto);
    }
}
