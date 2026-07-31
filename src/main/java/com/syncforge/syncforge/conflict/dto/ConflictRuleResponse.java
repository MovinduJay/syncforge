package com.syncforge.syncforge.conflict.dto;

import java.time.LocalDateTime;

public record ConflictRuleResponse(
        Long id,
        Long tenantId,
        String entityType,
        String fieldName,
        String owningIntegrationType,
        String strategy,
        boolean active,
        LocalDateTime createdAt
) {
}
