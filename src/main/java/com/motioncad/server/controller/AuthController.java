package com.motioncad.server.controller;

import com.motioncad.server.dto.AuthDTOs.*;
import com.motioncad.server.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Auth APIs for login and signup")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Sign up", description = "Register a new user with email, password, and nickname.")
    public void signUp(@RequestBody @Valid SignupRequestDTO dto) {
        authService.signUp(dto);
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user and return a JWT access token.")
    public TokenResponseDTO login(@RequestBody @Valid LoginRequestDTO dto) {
        return authService.login(dto);
    }
}
