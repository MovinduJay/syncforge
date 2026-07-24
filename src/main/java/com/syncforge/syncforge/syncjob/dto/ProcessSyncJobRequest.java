package com.syncforge.syncforge.syncjob.dto;

public record ProcessSyncJobRequest(
        boolean simulateFailure,
        String errorMessage
) {
}