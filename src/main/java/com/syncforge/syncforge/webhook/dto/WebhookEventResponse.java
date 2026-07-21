package com.syncforge.syncforge.webhook.dto;

import java.time.LocalDateTime;

public record WebhookEventResponse(
        Long id,
        Long tenantId,
        Long integrationId,
        String externalEventId,
        String eventType,
        String status,
        LocalDateTime receivedAt,
        LocalDateTime processedAt
) {
}