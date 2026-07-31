package com.syncforge.syncforge.integration.connector;

import com.syncforge.syncforge.syncjob.model.SyncJob;

public record SyncOperationContext(
        SyncJob syncJob,
        String sourceExternalEntityId,
        String targetExternalEntityId,
        String canonicalEntityId,
        String payloadJson
) {
}
