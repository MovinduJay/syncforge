package com.syncforge.syncforge.auth.controller;

import com.syncforge.syncforge.auth.dto.*;
import com.syncforge.syncforge.auth.model.AppUser;
import com.syncforge.syncforge.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    public MessageResponse logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return new MessageResponse("Logged out successfully");
    }

    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal AppUser user) {
        return new CurrentUserResponse(
                user.getId(),
                user.getTenant().getId(),
                user.getEmail(),
                user.getRole()
        );
    }
}
