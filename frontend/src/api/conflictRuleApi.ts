import type { ConflictRule } from "../types/conflictRule";

export async function getConflictRules(
    tenantId: number,
    accessToken: string
): Promise<ConflictRule[]> {
    const response = await fetch(`/api/tenants/${tenantId}/conflict-rules`, {
        headers: {
            Authorization: `Bearer ${accessToken}`,
        },
    });

    if (!response.ok) {
        throw new Error("Failed to fetch conflict rules");
    }

    return response.json();
}
