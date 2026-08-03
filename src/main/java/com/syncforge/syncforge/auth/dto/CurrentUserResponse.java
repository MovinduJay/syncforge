package com.syncforge.syncforge.auth.dto;

import com.syncforge.syncforge.auth.model.UserRole;

public record CurrentUserResponse(
        Long userId,
        Long tenantId,
        String email,
        UserRole role
) {
}
