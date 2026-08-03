import {
    createContext,
    useContext,
    useEffect,
    useMemo,
    useState,
    type ReactNode,
} from "react";
import {
    login as loginApi,
    logout as logoutApi,
    refreshAuthToken,
} from "../api/authApi";
import type { CurrentUser } from "../types/auth";

type AuthContextValue = {
    user: CurrentUser | null;
    accessToken: string | null;
    refreshToken: string | null;
    isAuthenticated: boolean;
    login: (email: string, password: string) => Promise<void>;
    logout: () => Promise<void>;
};

const AUTH_STORAGE_KEY = "syncforge_auth";

type StoredAuth = {
    user: CurrentUser;
    accessToken: string;
    refreshToken: string;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function loadStoredAuth(): StoredAuth | null {
    const storedValue = sessionStorage.getItem(AUTH_STORAGE_KEY);

    if (!storedValue) {
        return null;
    }

    try {
        return JSON.parse(storedValue) as StoredAuth;
    } catch {
        sessionStorage.removeItem(AUTH_STORAGE_KEY);
        return null;
    }
}

function saveStoredAuth(storedAuth: StoredAuth) {
    sessionStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(storedAuth));
}

function clearStoredAuth() {
    sessionStorage.removeItem(AUTH_STORAGE_KEY);
}

function getJwtExpiryTime(accessToken: string): number | null {
    try {
        const payloadBase64 = accessToken.split(".")[1];

        if (!payloadBase64) {
            return null;
        }

        const normalizedPayload = payloadBase64
            .replace(/-/g, "+")
            .replace(/_/g, "/");

        const payload = JSON.parse(atob(normalizedPayload));

        if (typeof payload.exp !== "number") {
            return null;
        }

        return payload.exp * 1000;
    } catch {
        return null;
    }
}

function buildCurrentUser(response: {
    userId: number;
    tenantId: number;
    email: string;
    role: CurrentUser["role"];
}): CurrentUser {
    return {
        userId: response.userId,
        tenantId: response.tenantId,
        email: response.email,
        role: response.role,
    };
}

export function AuthProvider({ children }: { children: ReactNode }) {
    const storedAuth = loadStoredAuth();

    const [user, setUser] = useState<CurrentUser | null>(
        storedAuth?.user ?? null
    );
    const [accessToken, setAccessToken] = useState<string | null>(
        storedAuth?.accessToken ?? null
    );
    const [refreshToken, setRefreshToken] = useState<string | null>(
        storedAuth?.refreshToken ?? null
    );

    async function login(email: string, password: string) {
        const response = await loginApi(email, password);

        const currentUser = buildCurrentUser(response);

        setUser(currentUser);
        setAccessToken(response.accessToken);
        setRefreshToken(response.refreshToken);

        saveStoredAuth({
            user: currentUser,
            accessToken: response.accessToken,
            refreshToken: response.refreshToken,
        });
    }

    async function refreshSession() {
        if (!refreshToken) {
            return;
        }

        try {
            const response = await refreshAuthToken(refreshToken);
            const currentUser = buildCurrentUser(response);

            setUser(currentUser);
            setAccessToken(response.accessToken);
            setRefreshToken(response.refreshToken);

            saveStoredAuth({
                user: currentUser,
                accessToken: response.accessToken,
                refreshToken: response.refreshToken,
            });
        } catch {
            setUser(null);
            setAccessToken(null);
            setRefreshToken(null);
            clearStoredAuth();
        }
    }

    async function logout() {
        if (refreshToken) {
            await logoutApi(refreshToken);
        }

        setUser(null);
        setAccessToken(null);
        setRefreshToken(null);
        clearStoredAuth();
    }

    useEffect(() => {
        if (!accessToken || !refreshToken) {
            return;
        }

        const expiryTime = getJwtExpiryTime(accessToken);

        if (!expiryTime) {
            return;
        }

        const refreshOneMinuteBeforeExpiry = expiryTime - Date.now() - 60_000;
        const refreshDelay = Math.max(refreshOneMinuteBeforeExpiry, 5_000);

        const timeoutId = window.setTimeout(() => {
            refreshSession();
        }, refreshDelay);

        return () => {
            window.clearTimeout(timeoutId);
        };
    }, [accessToken, refreshToken]);

    const value = useMemo(
        () => ({
            user,
            accessToken,
            refreshToken,
            isAuthenticated: Boolean(user && accessToken),
            login,
            logout,
        }),
        [user, accessToken, refreshToken]
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const context = useContext(AuthContext);

    if (!context) {
        throw new Error("useAuth must be used inside AuthProvider");
    }

    return context;
}
