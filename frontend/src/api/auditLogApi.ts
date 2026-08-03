import type { AuditLog } from "../types/auditLog";

export async function getAuditLogs(
    tenantId: number,
    accessToken: string
): Promise<AuditLog[]> {
    const response = await fetch(`/api/tenants/${tenantId}/audit-logs`, {
        headers: {
            Authorization: `Bearer ${accessToken}`,
        },
    });

    if (!response.ok) {
        throw new Error("Failed to fetch audit logs");
    }

    return response.json();
}
