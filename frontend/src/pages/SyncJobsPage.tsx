import { useQuery } from "@tanstack/react-query";
import { getSyncJobs } from "../api/syncJobApi";
import { useAuth } from "../auth/AuthContext";
import type { SyncJobStatus } from "../types/syncJob";

function getStatusClass(status: SyncJobStatus) {
    switch (status) {
        case "SUCCEEDED":
            return "status status-success";
        case "FAILED":
            return "status status-failed";
        case "DEAD_LETTER":
            return "status status-dead";
        case "PROCESSING":
            return "status status-processing";
        default:
            return "status";
    }
}

export function SyncJobsPage() {
    const { user, accessToken } = useAuth();

    const syncJobsQuery = useQuery({
        queryKey: ["sync-jobs", user?.tenantId],
        queryFn: () => getSyncJobs(user!.tenantId, accessToken!),
        enabled: Boolean(user?.tenantId && accessToken),
        refetchInterval: 5000,
    });

    const syncJobs = syncJobsQuery.data ?? [];

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
                    {syncJobs.map((job) => (
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
                    </tbody>
                </table>
            </section>
        </main>
    );
}
