export type WebhookEventStatus =
    | "RECEIVED"
    | "DUPLICATE"
    | "PROCESSED"
    | "FAILED";

export type WebhookEvent = {
    id: number;
    tenantId: number;
    integrationId: number;
    externalEventId: string;
    eventType: string;
    status: WebhookEventStatus;
    receivedAt: string;
    processedAt: string | null;
};
