import { useQuery } from "@tanstack/react-query";
import { getEntityMappings } from "../api/entityMappingApi";
import { useAuth } from "../auth/AuthContext";

export function EntityMappingsPage() {
  const { user, accessToken } = useAuth();

  const entityMappingsQuery = useQuery({
    queryKey: ["entity-mappings", user?.tenantId],
    queryFn: () => getEntityMappings(user!.tenantId, accessToken!),
    enabled: Boolean(user?.tenantId && accessToken),
  });

  const mappings = entityMappingsQuery.data ?? [];

  const canonicalIds = new Set(
    mappings.map((mapping) => mapping.canonicalEntityId)
  );

  const customerMappings = mappings.filter(
    (mapping) => mapping.entityType === "CUSTOMER"
  );

  return (
    <main>
      <section className="page-header">
        <div>
          <p className="eyebrow">Canonical Identity</p>
          <h1>Entity Mappings</h1>
          <p className="muted">
            Map external entity IDs from CRM, Billing, and Support systems into
            shared canonical SyncForge IDs.
          </p>
        </div>
      </section>

      <section className="grid stats-grid">
        <article className="card">
          <h2>Total Mappings</h2>
          <p className="metric">{mappings.length}</p>
        </article>

        <article className="card">
          <h2>Canonical IDs</h2>
          <p className="metric">{canonicalIds.size}</p>
        </article>

        <article className="card">
          <h2>Customer Mappings</h2>
          <p className="metric">{customerMappings.length}</p>
        </article>

        <article className="card">
          <h2>Tenant</h2>
          <p className="metric">{user?.tenantId}</p>
        </article>
      </section>

      {entityMappingsQuery.isLoading && (
        <p className="muted">Loading entity mappings...</p>
      )}

      {entityMappingsQuery.isError && (
        <p className="error">Failed to load entity mappings.</p>
      )}

      <section className="table-card">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>System</th>
              <th>Entity Type</th>
              <th>External Entity ID</th>
              <th>Canonical Entity ID</th>
            </tr>
          </thead>

          <tbody>
            {mappings.map((mapping) => (
              <tr key={mapping.id}>
                <td>{mapping.id}</td>
                <td>
                  <span className="badge">{mapping.integrationType}</span>
                </td>
                <td>{mapping.entityType}</td>
                <td>{mapping.externalEntityId}</td>
                <td>{mapping.canonicalEntityId}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </main>
  );
}
