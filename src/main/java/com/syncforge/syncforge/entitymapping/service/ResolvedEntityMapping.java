package com.syncforge.syncforge.entitymapping.service;

public record ResolvedEntityMapping(
        String sourceExternalEntityId,
        String targetExternalEntityId,
        String canonicalEntityId
) {
}
