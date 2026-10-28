import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { getSyncJobs } from "../api/syncJobApi";
import { useAuth } from "../auth/useAuth";
import type { SyncJobStatus } from "../types/syncJob";
import { getStatusClass } from "../utils/statusClass";

export function SyncJobsPage() {
    const { user, accessToken } = useAuth();
    const [search, setSearch] = useState("");
    const [statusFilter, setStatusFilter] = useState<SyncJobStatus | "ALL">("ALL");

    const syncJobsQuery = useQuery({
        queryKey: ["sync-jobs", user?.tenantId],
        queryFn: () => getSyncJobs(user!.tenantId, accessToken!),
        enabled: Boolean(user?.tenantId && accessToken),
        refetchInterval: 5000,
    });

    const syncJobs = syncJobsQuery.data ?? [];
    const normalizedSearch = search.trim().toLowerCase();
    const filteredJobs = syncJobs.filter((job) => {
        const matchesStatus = statusFilter === "ALL" || job.status === statusFilter;
        const matchesSearch = !normalizedSearch || [
            job.id,
            job.webhookEventId,
            job.entityType,
            job.operationType,
            job.lastError,
        ].some((value) => String(value ?? "").toLowerCase().includes(normalizedSearch));

        return matchesStatus && matchesSearch;
    });

    const totalJobs = syncJobs.length;
    const succeededJobs = syncJobs.filter((job) => job.status === "SUCCEEDED").length;
    const failedJobs = syncJobs.filter(
        (job) => job.status === "FAILED" || job.status === "DEAD_LETTER"
    ).length;
    const pendingJobs = syncJobs.filter(
        (job) => job.status === "PENDING" || job.status === "PROCESSING"
    ).length;

    return (
        <main>
            <section className="page-header">
                <div>
                    <p className="eyebrow">Async Pipeline</p>
                    <h1>Sync Jobs</h1>
                    <p className="muted">
                        Monitor downstream jobs created from webhook events and processed through RabbitMQ.
                    </p>
                </div>
            </section>

            <section className="grid stats-grid">
                <article className="card">
                    <h2>Total Jobs</h2>
                    <p className="metric">{totalJobs}</p>
                </article>

                <article className="card">
                    <h2>Succeeded</h2>
                    <p className="metric">{succeededJobs}</p>
                </article>

                <article className="card">
                    <h2>Pending / Processing</h2>
                    <p className="metric">{pendingJobs}</p>
                </article>

                <article className="card">
                    <h2>Failed / Dead Letter</h2>
                    <p className="metric">{failedJobs}</p>
                </article>
            </section>

            {syncJobsQuery.isLoading && <p className="muted">Loading sync jobs...</p>}

            {syncJobsQuery.isError && (
                <p className="error">Failed to load sync jobs.</p>
            )}

            <section className="table-toolbar" aria-label="Sync job filters">
                <label className="toolbar-field">
                    <span>Search</span>
                    <input
                        type="search"
                        placeholder="ID, entity, operation, or error"
                        value={search}
                        onChange={(event) => setSearch(event.target.value)}
                    />
                </label>
                <label className="toolbar-field">
                    <span>Status</span>
                    <select
                        value={statusFilter}
                        onChange={(event) => setStatusFilter(event.target.value as SyncJobStatus | "ALL")}
                    >
                        <option value="ALL">All statuses</option>
                        <option value="PENDING">Pending</option>
                        <option value="PROCESSING">Processing</option>
                        <option value="SUCCEEDED">Succeeded</option>
                        <option value="FAILED">Failed</option>
                        <option value="DEAD_LETTER">Dead letter</option>
                    </select>
                </label>
                <p className="toolbar-summary">Showing {filteredJobs.length} of {syncJobs.length} jobs</p>
            </section>

            <section className="table-card">
                <table>
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Webhook</th>
                        <th>Entity</th>
                        <th>Operation</th>
                        <th>Status</th>
                        <th>Attempts</th>
                        <th>Last Error</th>
                        <th>Created</th>
                    </tr>
                    </thead>

                    <tbody>
                    {filteredJobs.map((job) => (
                        <tr key={job.id}>
                            <td>{job.id}</td>
                            <td>{job.webhookEventId}</td>
                            <td>{job.entityType}</td>
                            <td>{job.operationType}</td>
                            <td>
                  <span className={getStatusClass(job.status)}>
                    {job.status}
                  </span>
                            </td>
                            <td>{job.attemptCount}</td>
                            <td>{job.lastError ?? "-"}</td>
                            <td>{new Date(job.createdAt).toLocaleString()}</td>
                        </tr>
                    ))}
                    {filteredJobs.length === 0 && !syncJobsQuery.isLoading && (
                        <tr><td colSpan={8} className="empty-state">No sync jobs match these filters.</td></tr>
                    )}
                    </tbody>
                </table>
            </section>
        </main>
    );
}
