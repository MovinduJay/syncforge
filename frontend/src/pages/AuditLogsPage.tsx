import { useQuery } from "@tanstack/react-query";
import { getAuditLogs } from "../api/auditLogApi";
import { useAuth } from "../auth/AuthContext";
import type { AuditAction } from "../types/auditLog";

function getActionClass(action: AuditAction) {
    if (action.includes("SUCCEEDED")) {
        return "status status-success";
    }

    if (action.includes("FAILED") || action.includes("DEAD_LETTER")) {
        return "status status-failed";
    }

    if (action.includes("PROCESSING")) {
        return "status status-processing";
    }

    return "status";
}

export function AuditLogsPage() {
    const { user, accessToken } = useAuth();

    const auditLogsQuery = useQuery({
        queryKey: ["audit-logs", user?.tenantId],
        queryFn: () => getAuditLogs(user!.tenantId, accessToken!),
        enabled: Boolean(user?.tenantId && accessToken),
        refetchInterval: 5000,
    });

    const auditLogs = auditLogsQuery.data ?? [];

    const webhookLogs = auditLogs.filter((log) =>
        log.action.startsWith("WEBHOOK")
    ).length;

    const syncJobLogs = auditLogs.filter((log) =>
        log.action.startsWith("SYNC_JOB")
    ).length;

    const failureLogs = auditLogs.filter(
        (log) => log.action.includes("FAILED") || log.action.includes("DEAD_LETTER")
    ).length;

    return (
        <main>
            <section className="page-header">
                <div>
                    <p className="eyebrow">Traceability</p>
                    <h1>Audit Logs</h1>
                    <p className="muted">
                        Review tenant-scoped system activity across webhooks, sync jobs,
                        retries, failures, and successful processing.
                    </p>
                </div>
            </section>

            <section className="grid stats-grid">
                <article className="card">
                    <h2>Total Logs</h2>
                    <p className="metric">{auditLogs.length}</p>
                </article>

                <article className="card">
                    <h2>Webhook Logs</h2>
                    <p className="metric">{webhookLogs}</p>
                </article>

                <article className="card">
                    <h2>Sync Job Logs</h2>
                    <p className="metric">{syncJobLogs}</p>
                </article>

                <article className="card">
                    <h2>Failure Logs</h2>
                    <p className="metric">{failureLogs}</p>
                </article>
            </section>

            {auditLogsQuery.isLoading && <p className="muted">Loading audit logs...</p>}

            {auditLogsQuery.isError && (
                <p className="error">Failed to load audit logs.</p>
            )}

            <section className="table-card">
                <table>
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Action</th>
                        <th>Message</th>
                        <th>Webhook</th>
                        <th>Sync Job</th>
                        <th>Created</th>
                    </tr>
                    </thead>

                    <tbody>
                    {auditLogs.map((log) => (
                        <tr key={log.id}>
                            <td>{log.id}</td>
                            <td>
                  <span className={getActionClass(log.action)}>
                    {log.action}
                  </span>
                            </td>
                            <td>{log.message}</td>
                            <td>{log.webhookEventId ?? "-"}</td>
                            <td>{log.syncJobId ?? "-"}</td>
                            <td>{new Date(log.createdAt).toLocaleString()}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </section>
        </main>
    );
}
