import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { getIntegrations } from "../api/integrationApi";
import { useAuth } from "../auth/AuthContext";

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
    const normalizedSearch = search.trim().toLowerCase();
    const filteredIntegrations = integrations.filter((integration) => {
        const matchesType = typeFilter === "ALL" || integration.type === typeFilter;
        const matchesSearch = !normalizedSearch || [
            integration.id,
            integration.displayName,
            integration.status,
        ].some((value) => String(value).toLowerCase().includes(normalizedSearch));

        return matchesType && matchesSearch;
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

            <section className="table-toolbar" aria-label="Integration filters">
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
            </section>

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
                            <td>{new Date(integration.createdAt).toLocaleString()}</td>
                        </tr>
                    ))}
                    {filteredIntegrations.length === 0 && !integrationsQuery.isLoading && (
                        <tr><td colSpan={5} className="empty-state">No integrations match these filters.</td></tr>
                    )}
                    </tbody>
                </table>
            </section>
        </main>
    );
}
