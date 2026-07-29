package com.syncforge.syncforge.integration.connector;

public record SyncOperationResult(
        boolean success,
        String message
) {
}
