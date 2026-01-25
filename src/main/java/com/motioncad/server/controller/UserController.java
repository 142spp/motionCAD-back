package com.motioncad.server.controller;

import com.motioncad.server.domain.User;
import com.motioncad.server.domain.UserSettings;
import com.motioncad.server.dto.UserResponseDTO;
import com.motioncad.server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
    public void updateUserSettings(@PathVariable Long userId, @RequestBody UserSettings settings) {
        userService.updateUserSettings(userId, settings);
    }
}
