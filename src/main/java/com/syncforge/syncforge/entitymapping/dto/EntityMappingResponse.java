package com.syncforge.syncforge.entitymapping.dto;

import java.time.LocalDateTime;

public record EntityMappingResponse(
        Long id,
        Long tenantId,
        Long integrationId,
        String integrationType,
        String entityType,
        String externalEntityId,
        String canonicalEntityId,
        LocalDateTime createdAt
) {
}
