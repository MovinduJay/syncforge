import { useQuery } from "@tanstack/react-query";
import { getWebhookEvents } from "../api/webhookEventApi";
import { useAuth } from "../auth/AuthContext";
import type { WebhookEventStatus } from "../types/webhookEvent";

function getStatusClass(status: WebhookEventStatus) {
    switch (status) {
        case "PROCESSED":
            return "status status-success";
        case "FAILED":
            return "status status-failed";
        case "DUPLICATE":
            return "status status-processing";
        default:
            return "status";
    }
}

export function WebhookEventsPage() {
    const { user, accessToken } = useAuth();

    const webhookEventsQuery = useQuery({
        queryKey: ["webhook-events", user?.tenantId],
        queryFn: () => getWebhookEvents(user!.tenantId, accessToken!),
        enabled: Boolean(user?.tenantId && accessToken),
        refetchInterval: 5000,
    });

    const webhookEvents = webhookEventsQuery.data ?? [];

    const receivedCount = webhookEvents.filter(
        (event) => event.status === "RECEIVED"
    ).length;

    const duplicateCount = webhookEvents.filter(
        (event) => event.status === "DUPLICATE"
    ).length;

    const failedCount = webhookEvents.filter(
        (event) => event.status === "FAILED"
    ).length;

    return (
        <main>
            <section className="page-header">
                <div>
                    <p className="eyebrow">Webhook Ingestion</p>
                    <h1>Webhook Events</h1>
                    <p className="muted">
                        Inspect tenant-scoped webhook deliveries received from external
                        systems before they become downstream sync jobs.
                    </p>
                </div>
            </section>

            <section className="grid stats-grid">
                <article className="card">
                    <h2>Total Events</h2>
                    <p className="metric">{webhookEvents.length}</p>
                </article>

                <article className="card">
                    <h2>Received</h2>
                    <p className="metric">{receivedCount}</p>
                </article>

                <article className="card">
                    <h2>Duplicates</h2>
                    <p className="metric">{duplicateCount}</p>
                </article>

                <article className="card">
                    <h2>Failed</h2>
                    <p className="metric">{failedCount}</p>
                </article>
            </section>

            {webhookEventsQuery.isLoading && (
                <p className="muted">Loading webhook events...</p>
            )}

            {webhookEventsQuery.isError && (
                <p className="error">Failed to load webhook events.</p>
            )}

            <section className="table-card">
                <table>
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Integration</th>
                        <th>External Event ID</th>
                        <th>Event Type</th>
                        <th>Status</th>
                        <th>Received</th>
                        <th>Processed</th>
                    </tr>
                    </thead>

                    <tbody>
                    {webhookEvents.map((event) => (
                        <tr key={event.id}>
                            <td>{event.id}</td>
                            <td>{event.integrationId}</td>
                            <td>{event.externalEventId}</td>
                            <td>{event.eventType}</td>
                            <td>
                  <span className={getStatusClass(event.status)}>
                    {event.status}
                  </span>
                            </td>
                            <td>{new Date(event.receivedAt).toLocaleString()}</td>
                            <td>
                                {event.processedAt
                                    ? new Date(event.processedAt).toLocaleString()
                                    : "-"}
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </section>
        </main>
    );
}
