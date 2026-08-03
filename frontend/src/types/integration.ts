export type Integration = {
    id: number;
    tenantId: number;
    type: "CRM" | "BILLING" | "SUPPORT";
    displayName: string;
    status: string;
    createdAt: string;
};
