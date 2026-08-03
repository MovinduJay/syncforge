import { useQuery } from "@tanstack/react-query";
import { getConflictRules } from "../api/conflictRuleApi";
import { useAuth } from "../auth/AuthContext";
import type { ConflictStrategy } from "../types/conflictRule";

function getStrategyClass(strategy: ConflictStrategy) {
    switch (strategy) {
        case "OWNER_WINS":
            return "status status-success";
        case "MANUAL_REVIEW":
            return "status status-processing";
        case "IGNORE":
            return "status status-failed";
        default:
            return "status";
    }
}

export function ConflictRulesPage() {
    const { user, accessToken } = useAuth();

    const conflictRulesQuery = useQuery({
        queryKey: ["conflict-rules", user?.tenantId],
        queryFn: () => getConflictRules(user!.tenantId, accessToken!),
        enabled: Boolean(user?.tenantId && accessToken),
    });

    const rules = conflictRulesQuery.data ?? [];

    const activeRules = rules.filter((rule) => rule.active).length;

    const ownerWinsRules = rules.filter(
        (rule) => rule.strategy === "OWNER_WINS"
    ).length;

    const manualReviewRules = rules.filter(
        (rule) => rule.strategy === "MANUAL_REVIEW"
    ).length;

    return (
        <main>
            <section className="page-header">
                <div>
                    <p className="eyebrow">Conflict Resolution</p>
                    <h1>Conflict Rules</h1>
                    <p className="muted">
                        Define which external system owns specific fields and how SyncForge
                        should handle conflicting updates.
                    </p>
                </div>
            </section>

            <section className="grid stats-grid">
                <article className="card">
                    <h2>Total Rules</h2>
                    <p className="metric">{rules.length}</p>
                </article>

                <article className="card">
                    <h2>Active Rules</h2>
                    <p className="metric">{activeRules}</p>
                </article>

                <article className="card">
                    <h2>Owner Wins</h2>
                    <p className="metric">{ownerWinsRules}</p>
                </article>

                <article className="card">
                    <h2>Manual Review</h2>
                    <p className="metric">{manualReviewRules}</p>
                </article>
            </section>

            {conflictRulesQuery.isLoading && (
                <p className="muted">Loading conflict rules...</p>
            )}

            {conflictRulesQuery.isError && (
                <p className="error">Failed to load conflict rules.</p>
            )}

            <section className="table-card">
                <table>
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Entity Type</th>
                        <th>Field</th>
                        <th>Owner</th>
                        <th>Strategy</th>
                        <th>Status</th>
                        <th>Created</th>
                    </tr>
                    </thead>

                    <tbody>
                    {rules.map((rule) => (
                        <tr key={rule.id}>
                            <td>{rule.id}</td>
                            <td>{rule.entityType}</td>
                            <td>{rule.fieldName}</td>
                            <td>
                                <span className="badge">{rule.owningIntegrationType}</span>
                            </td>
                            <td>
                  <span className={getStrategyClass(rule.strategy)}>
                    {rule.strategy}
                  </span>
                            </td>
                            <td>{rule.active ? "ACTIVE" : "INACTIVE"}</td>
                            <td>{new Date(rule.createdAt).toLocaleString()}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </section>
        </main>
    );
}
