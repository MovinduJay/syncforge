export type AuditAction =
    | "WEBHOOK_RECEIVED"
    | "WEBHOOK_DUPLICATE"
    | "SYNC_JOB_CREATED"
    | "SYNC_JOB_PROCESSING"
    | "SYNC_JOB_SUCCEEDED"
    | "SYNC_JOB_FAILED"
    | "SYNC_JOB_DEAD_LETTER";

export type AuditLog = {
    id: number;
    tenantId: number;
    webhookEventId: number | null;
    syncJobId: number | null;
    action: AuditAction;
    message: string;
    detailsJson: string | null;
    createdAt: string;
};
