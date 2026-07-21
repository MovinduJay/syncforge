package com.syncforge.syncforge.integration.dto;

import java.time.LocalDateTime;

public record IntegrationResponse(
        Long id,
        Long tenantId,
        String type,
        String displayName,
        String status,
        LocalDateTime createdAt
) {
}