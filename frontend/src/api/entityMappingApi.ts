import type { EntityMapping } from "../types/entityMapping";

export async function getEntityMappings(
    tenantId: number,
    accessToken: string
): Promise<EntityMapping[]> {
    const response = await fetch(`/api/tenants/${tenantId}/entity-mappings`, {
        headers: {
            Authorization: `Bearer ${accessToken}`,
        },
    });

    if (!response.ok) {
        throw new Error("Failed to fetch entity mappings");
    }

    return response.json();
}
