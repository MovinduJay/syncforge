import { useQuery } from "@tanstack/react-query";
import { getIntegrations } from "../api/integrationApi";
import { useAuth } from "../auth/AuthContext";

export function IntegrationsPage() {
    const { user, accessToken } = useAuth();

    const integrationsQuery = useQuery({
        queryKey: ["integrations", user?.tenantId],
        queryFn: () => getIntegrations(user!.tenantId, accessToken!),
        enabled: Boolean(user?.tenantId && accessToken),
    });

    const integrations = integrationsQuery.data ?? [];

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
                    {integrations.map((integration) => (
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
                    </tbody>
                </table>
            </section>
        </main>
    );
}
