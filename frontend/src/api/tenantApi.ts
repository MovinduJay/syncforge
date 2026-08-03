import type { Tenant } from "../types/tenant";

export async function getCurrentTenant(accessToken: string): Promise<Tenant> {
    const response = await fetch("/api/tenants", {
        headers: {
            Authorization: `Bearer ${accessToken}`,
        },
    });

    if (!response.ok) {
        throw new Error("Failed to fetch tenant");
    }

    const tenants: Tenant[] = await response.json();

    if (tenants.length === 0) {
        throw new Error("No tenant found");
    }

    return tenants[0];
}
