export type EntityMapping = {
    id: number;
    tenantId: number;
    integrationId: number;
    integrationType: "CRM" | "BILLING" | "SUPPORT";
    entityType: string;
    externalEntityId: string;
    canonicalEntityId: string;
};
