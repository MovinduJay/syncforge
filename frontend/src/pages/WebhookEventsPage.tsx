import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { getWebhookEvents } from "../api/webhookEventApi";
import { useAuth } from "../auth/useAuth";
import type { WebhookEventStatus } from "../types/webhookEvent";
import { getStatusClass } from "../utils/statusClass";
import { formatDateTime } from "../utils/formatDateTime";
import { ACTIVITY_REFRESH_INTERVAL_MS } from "../config/query";
import { TableEmptyState } from "../components/TableEmptyState";
import { TableToolbar } from "../components/TableToolbar";
import { matchesSearch } from "../utils/matchesSearch";

export function WebhookEventsPage() {
    const { user, accessToken } = useAuth();
    const [search, setSearch] = useState("");
    const [statusFilter, setStatusFilter] = useState<WebhookEventStatus | "ALL">("ALL");

    const webhookEventsQuery = useQuery({
        queryKey: ["webhook-events", user?.tenantId],
        queryFn: () => getWebhookEvents(user!.tenantId, accessToken!),
        enabled: Boolean(user?.tenantId && accessToken),
        refetchInterval: ACTIVITY_REFRESH_INTERVAL_MS,
    });

    const webhookEvents = webhookEventsQuery.data ?? [];
    const filteredEvents = webhookEvents.filter((event) => {
        const matchesStatus = statusFilter === "ALL" || event.status === statusFilter;
        const matchesQuery = matchesSearch(search, [
            event.id,
            event.integrationId,
            event.externalEventId,
            event.eventType,
        ]);

        return matchesStatus && matchesQuery;
    });

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

            <TableToolbar label="Webhook event filters">
                <label className="toolbar-field">
                    <span>Search</span>
                    <input
                        type="search"
                        placeholder="Event ID, type, or integration"
                        value={search}
                        onChange={(event) => setSearch(event.target.value)}
                    />
                </label>
                <label className="toolbar-field">
                    <span>Status</span>
                    <select
                        value={statusFilter}
                        onChange={(event) => setStatusFilter(event.target.value as WebhookEventStatus | "ALL")}
                    >
                        <option value="ALL">All statuses</option>
                        <option value="RECEIVED">Received</option>
                        <option value="PROCESSED">Processed</option>
                        <option value="DUPLICATE">Duplicate</option>
                        <option value="FAILED">Failed</option>
                    </select>
                </label>
                <p className="toolbar-summary">Showing {filteredEvents.length} of {webhookEvents.length} events</p>
            </TableToolbar>

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
                    {filteredEvents.map((event) => (
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
                            <td>{formatDateTime(event.receivedAt)}</td>
                            <td>{formatDateTime(event.processedAt)}</td>
                        </tr>
                    ))}
                    {filteredEvents.length === 0 && !webhookEventsQuery.isLoading && (
                        <TableEmptyState columnCount={7} message="No webhook events match these filters." />
                    )}
                    </tbody>
                </table>
            </section>
        </main>
    );
}
