package com.syncforge.syncforge.audit.model;

public enum AuditAction {
    WEBHOOK_RECEIVED,
    WEBHOOK_DUPLICATE,
    SYNC_JOB_CREATED,
    SYNC_JOB_PROCESSING,
    SYNC_JOB_SUCCEEDED,
    SYNC_JOB_FAILED,
    SYNC_JOB_DEAD_LETTER
}