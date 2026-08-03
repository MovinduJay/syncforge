export type SyncJobStatus =
    | "PENDING"
    | "PROCESSING"
    | "SUCCEEDED"
    | "FAILED"
    | "DEAD_LETTER";

export type SyncJob = {
    id: number;
    tenantId: number;
    webhookEventId: number;
    sourceIntegrationId: number;
    targetIntegrationId: number;
    entityType: string;
    operationType: string;
    status: SyncJobStatus;
    attemptCount: number;
    lastError: string | null;
    nextRetryAt: string | null;
    createdAt: string;
    updatedAt: string;
    processedAt: string | null;
};
