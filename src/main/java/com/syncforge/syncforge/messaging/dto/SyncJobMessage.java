package com.syncforge.syncforge.messaging.dto;

public record SyncJobMessage(
        Long tenantId,
        Long syncJobId
) {
}