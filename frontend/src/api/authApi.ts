import type { AuthResponse, CurrentUser } from "../types/auth";

export async function login(email: string, password: string): Promise<AuthResponse> {
    const response = await fetch("/api/auth/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, password }),
    });

    if (!response.ok) {
        throw new Error("Invalid email or password");
    }

    return response.json();
}

export async function getCurrentUser(accessToken: string): Promise<CurrentUser> {
    const response = await fetch("/api/auth/me", {
        headers: {
            Authorization: `Bearer ${accessToken}`,
        },
    });

    if (!response.ok) {
        throw new Error("Failed to load current user");
    }

    return response.json();
}

export async function logout(refreshToken: string): Promise<void> {
    await fetch("/api/auth/logout", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({ refreshToken }),
    });
}

export async function refreshAuthToken(
    refreshToken: string
): Promise<AuthResponse> {
    const response = await fetch("/api/auth/refresh", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({ refreshToken }),
    });

    if (!response.ok) {
        throw new Error("Failed to refresh token");
    }

    return response.json();
}
