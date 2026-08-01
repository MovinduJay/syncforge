package com.syncforge.syncforge.auth.dto;

import com.syncforge.syncforge.auth.model.UserRole;

public record AuthResponse(
        Long userId,
        Long tenantId,
        String email,
        UserRole role,
        String accessToken,
        String refreshToken
) {
}
