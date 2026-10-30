import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { getIntegrations } from "../api/integrationApi";
import { useAuth } from "../auth/useAuth";
import { formatDateTime } from "../utils/formatDateTime";
import { TableEmptyState } from "../components/TableEmptyState";
import { TableToolbar } from "../components/TableToolbar";
import { matchesSearch } from "../utils/matchesSearch";

export function IntegrationsPage() {
    const { user, accessToken } = useAuth();
    const [search, setSearch] = useState("");
    const [typeFilter, setTypeFilter] = useState<"ALL" | "CRM" | "BILLING" | "SUPPORT">("ALL");

    const integrationsQuery = useQuery({
        queryKey: ["integrations", user?.tenantId],
        queryFn: () => getIntegrations(user!.tenantId, accessToken!),
        enabled: Boolean(user?.tenantId && accessToken),
    });

    const integrations = integrationsQuery.data ?? [];
    const filteredIntegrations = integrations.filter((integration) => {
        const matchesType = typeFilter === "ALL" || integration.type === typeFilter;
        const matchesQuery = matchesSearch(search, [
            integration.id,
            integration.displayName,
            integration.status,
        ]);

        return matchesType && matchesQuery;
    });
    const activeCount = integrations.filter((integration) => integration.status === "ACTIVE").length;
    const integrationTypes = new Set(integrations.map((integration) => integration.type)).size;

    return (
        <main>
            <section className="page-header">
                <div>
                    <p className="eyebrow">Connected Systems</p>
                    <h1>Integrations</h1>
                    <p className="muted">
                        View tenant-scoped external systems connected to SyncForge.
                    </p>
                </div>
            </section>

            {integrationsQuery.isLoading && <p className="muted">Loading integrations...</p>}

            {integrationsQuery.isError && (
                <p className="error">Failed to load integrations.</p>
            )}

            <section className="grid stats-grid">
                <article className="card"><h2>Total</h2><p className="metric">{integrations.length}</p></article>
                <article className="card"><h2>Active</h2><p className="metric">{activeCount}</p></article>
                <article className="card"><h2>Inactive</h2><p className="metric">{integrations.length - activeCount}</p></article>
                <article className="card"><h2>System Types</h2><p className="metric">{integrationTypes}</p></article>
            </section>

            <TableToolbar label="Integration filters">
                <label className="toolbar-field">
                    <span>Search</span>
                    <input
                        type="search"
                        placeholder="Name, status, or ID"
                        value={search}
                        onChange={(event) => setSearch(event.target.value)}
                    />
                </label>
                <label className="toolbar-field">
                    <span>System type</span>
                    <select value={typeFilter} onChange={(event) => setTypeFilter(event.target.value as typeof typeFilter)}>
                        <option value="ALL">All types</option>
                        <option value="CRM">CRM</option>
                        <option value="BILLING">Billing</option>
                        <option value="SUPPORT">Support</option>
                    </select>
                </label>
                <p className="toolbar-summary">Showing {filteredIntegrations.length} of {integrations.length}</p>
            </TableToolbar>

            <section className="table-card">
                <table>
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Type</th>
                        <th>Name</th>
                        <th>Status</th>
                        <th>Created At</th>
                    </tr>
                    </thead>

                    <tbody>
                    {filteredIntegrations.map((integration) => (
                        <tr key={integration.id}>
                            <td>{integration.id}</td>
                            <td>
                                <span className="badge">{integration.type}</span>
                            </td>
                            <td>{integration.displayName}</td>
                            <td>{integration.status}</td>
                            <td>{formatDateTime(integration.createdAt)}</td>
                        </tr>
                    ))}
                    {filteredIntegrations.length === 0 && !integrationsQuery.isLoading && (
                        <TableEmptyState columnCount={5} message="No integrations match these filters." />
                    )}
                    </tbody>
                </table>
            </section>
        </main>
    );
}
