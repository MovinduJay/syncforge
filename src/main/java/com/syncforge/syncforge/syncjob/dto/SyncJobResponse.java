package com.syncforge.syncforge.syncjob.dto;

import java.time.LocalDateTime;

public record SyncJobResponse(
        Long id,
        Long tenantId,
        Long webhookEventId,
        Long sourceIntegrationId,
        Long targetIntegrationId,
        String entityType,
        String operationType,
        String status,
        int attemptCount,
        String lastError,
        LocalDateTime nextRetryAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime processedAt
) {
}