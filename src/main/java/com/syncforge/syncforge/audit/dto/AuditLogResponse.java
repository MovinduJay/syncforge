package com.syncforge.syncforge.audit.dto;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        Long tenantId,
        Long webhookEventId,
        Long syncJobId,
        String action,
        String message,
        String detailsJson,
        LocalDateTime createdAt
) {
}