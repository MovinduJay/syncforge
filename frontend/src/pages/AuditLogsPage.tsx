import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { getAuditLogs } from "../api/auditLogApi";
import { useAuth } from "../auth/useAuth";
import type { AuditAction } from "../types/auditLog";
import { downloadCsv } from "../utils/downloadCsv";

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
    const [search, setSearch] = useState("");
    const [category, setCategory] = useState<"ALL" | "WEBHOOK" | "SYNC_JOB" | "FAILURE">("ALL");

    const auditLogsQuery = useQuery({
        queryKey: ["audit-logs", user?.tenantId],
        queryFn: () => getAuditLogs(user!.tenantId, accessToken!),
        enabled: Boolean(user?.tenantId && accessToken),
        refetchInterval: 5000,
    });

    const auditLogs = auditLogsQuery.data ?? [];
    const normalizedSearch = search.trim().toLowerCase();
    const filteredLogs = auditLogs.filter((log) => {
        const matchesCategory = category === "ALL"
            || (category === "WEBHOOK" && log.action.startsWith("WEBHOOK"))
            || (category === "SYNC_JOB" && log.action.startsWith("SYNC_JOB"))
            || (category === "FAILURE" && (log.action.includes("FAILED") || log.action.includes("DEAD_LETTER")));
        const matchesSearch = !normalizedSearch || [
            log.id,
            log.action,
            log.message,
            log.webhookEventId,
            log.syncJobId,
        ].some((value) => String(value ?? "").toLowerCase().includes(normalizedSearch));

        return matchesCategory && matchesSearch;
    });

    const webhookLogs = auditLogs.filter((log) =>
        log.action.startsWith("WEBHOOK")
    ).length;

    const syncJobLogs = auditLogs.filter((log) =>
        log.action.startsWith("SYNC_JOB")
    ).length;

    const failureLogs = auditLogs.filter(
        (log) => log.action.includes("FAILED") || log.action.includes("DEAD_LETTER")
    ).length;

    function exportLogs() {
        downloadCsv(
            `syncforge-audit-logs-${new Date().toISOString().slice(0, 10)}.csv`,
            ["ID", "Action", "Message", "Webhook Event ID", "Sync Job ID", "Created At"],
            filteredLogs.map((log) => [
                log.id,
                log.action,
                log.message,
                log.webhookEventId,
                log.syncJobId,
                log.createdAt,
            ]),
        );
    }

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

            <section className="table-toolbar" aria-label="Audit log filters">
                <label className="toolbar-field">
                    <span>Search</span>
                    <input
                        type="search"
                        placeholder="Action, message, webhook, or job"
                        value={search}
                        onChange={(event) => setSearch(event.target.value)}
                    />
                </label>
                <label className="toolbar-field">
                    <span>Category</span>
                    <select
                        value={category}
                        onChange={(event) => setCategory(event.target.value as typeof category)}
                    >
                        <option value="ALL">All activity</option>
                        <option value="WEBHOOK">Webhooks</option>
                        <option value="SYNC_JOB">Sync jobs</option>
                        <option value="FAILURE">Failures</option>
                    </select>
                </label>
                <p className="toolbar-summary">Showing {filteredLogs.length} of {auditLogs.length} logs</p>
                <button
                    type="button"
                    className="secondary-button"
                    disabled={filteredLogs.length === 0}
                    onClick={exportLogs}
                >
                    Export CSV
                </button>
            </section>

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
                    {filteredLogs.map((log) => (
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
                    {filteredLogs.length === 0 && !auditLogsQuery.isLoading && (
                        <tr><td colSpan={6} className="empty-state">No audit logs match these filters.</td></tr>
                    )}
                    </tbody>
                </table>
            </section>
        </main>
    );
}
