export type UserRole = "ADMIN" | "VIEWER";

export type AuthResponse = {
    userId: number;
    tenantId: number;
    email: string;
    role: UserRole;
    accessToken: string;
    refreshToken: string;
};

export type CurrentUser = {
    userId: number;
    tenantId: number;
    email: string;
    role: UserRole;
};
