export type ConflictStrategy =
    | "OWNER_WINS"
    | "SOURCE_WINS"
    | "MANUAL_REVIEW"
    | "IGNORE";

export type IntegrationType = "CRM" | "BILLING" | "SUPPORT";

export type ConflictRule = {
    id: number;
    tenantId: number;
    entityType: string;
    fieldName: string;
    owningIntegrationType: IntegrationType;
    strategy: ConflictStrategy;
    active: boolean;
    createdAt: string;
};
