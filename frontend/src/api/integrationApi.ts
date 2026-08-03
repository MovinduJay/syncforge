import type { Integration } from "../types/integration";

export async function getIntegrations(
    tenantId: number,
    accessToken: string
): Promise<Integration[]> {
    const response = await fetch(`/api/tenants/${tenantId}/integrations`, {
        headers: {
            Authorization: `Bearer ${accessToken}`,
        },
    });

    if (!response.ok) {
        throw new Error("Failed to fetch integrations");
    }

    return response.json();
}
