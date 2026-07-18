package com.syncforge.syncforge.tenant.dto;

import java.time.LocalDateTime;

public record TenantResponse(
        Long id,
        String companyName,
        String status,
        LocalDateTime createdAt
) {
}