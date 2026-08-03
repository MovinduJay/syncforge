import type { WebhookEvent } from "../types/webhookEvent";

export async function getWebhookEvents(
    tenantId: number,
    accessToken: string
): Promise<WebhookEvent[]> {
    const response = await fetch(`/api/tenants/${tenantId}/webhook-events`, {
        headers: {
            Authorization: `Bearer ${accessToken}`,
        },
    });

    if (!response.ok) {
        throw new Error("Failed to fetch webhook events");
    }

    return response.json();
}

export async function sendWebhookEvent(
    integrationId: number,
    accessToken: string,
    externalEventId: string,
    eventType: string,
    payloadJson: string
): Promise<WebhookEvent> {
    const response = await fetch(`/api/integrations/${integrationId}/webhooks`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${accessToken}`,
        },
        body: JSON.stringify({
            externalEventId,
            eventType,
            payloadJson,
        }),
    });

    if (!response.ok) {
        throw new Error("Failed to send webhook event");
    }

    return response.json();
}
