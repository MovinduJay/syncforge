import type { SyncJob } from "../types/syncJob";

export async function getSyncJobs(
    tenantId: number,
    accessToken: string
): Promise<SyncJob[]> {
    const response = await fetch(`/api/tenants/${tenantId}/sync-jobs`, {
        headers: {
            Authorization: `Bearer ${accessToken}`,
        },
    });

    if (!response.ok) {
        throw new Error("Failed to fetch sync jobs");
    }

    return response.json();
}
