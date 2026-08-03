import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { sendWebhookEvent } from "../api/webhookEventApi";
import type { Integration } from "../types/integration";

type TestWebhookPanelProps = {
    integrations: Integration[];
    accessToken: string;
};

export function TestWebhookPanel({
                                     integrations,
                                     accessToken,
                                 }: TestWebhookPanelProps) {
    const queryClient = useQueryClient();

    const crmIntegration = integrations.find(
        (integration) => integration.type === "CRM"
    );

    const [externalEventId, setExternalEventId] = useState(
        `evt-ui-${Date.now()}`
    );
    const [eventType, setEventType] = useState("CUSTOMER_UPDATED");
    const [payloadJson, setPayloadJson] = useState(
        '{"customerId":"CRM-101","email":"ui-test@test.com"}'
    );

    const sendWebhookMutation = useMutation({
        mutationFn: () => {
            if (!crmIntegration) {
                throw new Error("CRM integration not found");
            }

            return sendWebhookEvent(
                crmIntegration.id,
                accessToken,
                externalEventId,
                eventType,
                payloadJson
            );
        },
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["webhook-events"] });
            await queryClient.invalidateQueries({ queryKey: ["sync-jobs"] });
            await queryClient.invalidateQueries({ queryKey: ["audit-logs"] });

            setExternalEventId(`evt-ui-${Date.now()}`);
        },
    });

    return (
        <section className="section">
            <div className="section-header">
                <div>
                    <p className="eyebrow">Demo Action</p>
                    <h2>Send Test Webhook</h2>
                    <p className="muted">
                        Trigger a CRM webhook from the UI and watch sync jobs and audit logs
                        update.
                    </p>
                </div>
            </div>

            <div className="form-card">
                <label>
                    Source Integration
                    <input
                        value={
                            crmIntegration
                                ? `${crmIntegration.displayName} (#${crmIntegration.id})`
                                : "CRM integration not found"
                        }
                        disabled
                    />
                </label>

                <label>
                    External Event ID
                    <input
                        value={externalEventId}
                        onChange={(event) => setExternalEventId(event.target.value)}
                    />
                </label>

                <label>
                    Event Type
                    <input
                        value={eventType}
                        onChange={(event) => setEventType(event.target.value)}
                    />
                </label>

                <label>
                    Payload JSON
                    <textarea
                        value={payloadJson}
                        onChange={(event) => setPayloadJson(event.target.value)}
                        rows={5}
                    />
                </label>

                {sendWebhookMutation.isError && (
                    <p className="error">Failed to send webhook. Check payload or token.</p>
                )}

                {sendWebhookMutation.isSuccess && (
                    <p className="success">Webhook sent successfully.</p>
                )}

                <button
                    onClick={() => sendWebhookMutation.mutate()}
                    disabled={!crmIntegration || sendWebhookMutation.isPending}
                >
                    {sendWebhookMutation.isPending ? "Sending..." : "Send Webhook"}
                </button>
            </div>
        </section>
    );
}
