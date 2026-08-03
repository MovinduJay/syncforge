import { useQuery } from "@tanstack/react-query";
import { getIntegrations } from "../api/integrationApi";
import { getCurrentTenant } from "../api/tenantApi";
import { useAuth } from "../auth/AuthContext";
import { TestWebhookPanel } from "../components/TestWebhookPanel";

export function DashboardPage() {
    const { user, accessToken } = useAuth();

    const tenantQuery = useQuery({
        queryKey: ["current-tenant"],
        queryFn: () => getCurrentTenant(accessToken!),
        enabled: Boolean(accessToken),
    });

    const integrationsQuery = useQuery({
        queryKey: ["integrations", user?.tenantId],
        queryFn: () => getIntegrations(user!.tenantId, accessToken!),
        enabled: Boolean(accessToken && user?.tenantId),
    });

    const integrations = integrationsQuery.data ?? [];

    const activeIntegrations = integrations.filter(
        (integration) => integration.status === "ACTIVE"
    );

    return (

        <main className="dashboard-page">
            <section className="dashboard-header">
                <div>
                    <p className="eyebrow">SyncForge</p>
                    <h1>Dashboard</h1>
                    <p className="muted">
                        Multi-tenant SaaS data sync monitoring console.
                    </p>
                </div>

            </section>

            {tenantQuery.isLoading && <p className="muted">Loading dashboard...</p>}

            {tenantQuery.isError && (
                <p className="error">Failed to load tenant data.</p>
            )}

            <section className="grid">
                <article className="card">
                    <h2>Current Tenant</h2>
                    <p>Company: {tenantQuery.data?.companyName ?? "Loading..."}</p>
                    <p>Status: {tenantQuery.data?.status ?? "-"}</p>
                    <p>Tenant ID: {tenantQuery.data?.id ?? "-"}</p>
                </article>

                <article className="card">
                    <h2>Current User</h2>
                    <p>Email: {user?.email}</p>
                    <p>Role: {user?.role}</p>
                    <p>Tenant ID: {user?.tenantId}</p>
                </article>

                <article className="card">
                    <h2>Integrations</h2>
                    <p>Total: {integrations.length}</p>
                    <p>Active: {activeIntegrations.length}</p>
                    <p>
                        Types:{" "}
                        {integrations.length > 0
                            ? integrations.map((integration) => integration.type).join(", ")
                            : "Loading..."}
                    </p>
                </article>
            </section>

            <section className="section">
                <div className="section-header">
                    <div>
                        <p className="eyebrow">Connected Systems</p>
                        <h2>Integrations</h2>
                    </div>
                </div>

                {integrationsQuery.isLoading && (
                    <p className="muted">Loading integrations...</p>
                )}

                {integrationsQuery.isError && (
                    <p className="error">Failed to load integrations.</p>
                )}

                <div className="integration-grid">
                    {integrations.map((integration) => (
                        <article className="card" key={integration.id}>
                            <p className="badge">{integration.type}</p>
                            <h3>{integration.displayName}</h3>
                            <p>Status: {integration.status}</p>
                            <p>Integration ID: {integration.id}</p>
                        </article>
                    ))}
                </div>
            </section>
            {accessToken && integrations.length > 0 && (
                <TestWebhookPanel integrations={integrations} accessToken={accessToken} />
            )}
        </main>
    );
}
