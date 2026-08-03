import {
    createContext,
    useContext,
    useMemo,
    useState,
    type ReactNode,
} from "react";
import { login as loginApi, logout as logoutApi } from "../api/authApi";
import type { CurrentUser } from "../types/auth";

type AuthContextValue = {
    user: CurrentUser | null;
    accessToken: string | null;
    refreshToken: string | null;
    isAuthenticated: boolean;
    login: (email: string, password: string) => Promise<void>;
    logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
    const [user, setUser] = useState<CurrentUser | null>(null);
    const [accessToken, setAccessToken] = useState<string | null>(null);
    const [refreshToken, setRefreshToken] = useState<string | null>(null);

    async function login(email: string, password: string) {
        const response = await loginApi(email, password);

        setUser({
            userId: response.userId,
            tenantId: response.tenantId,
            email: response.email,
            role: response.role,
        });

        setAccessToken(response.accessToken);
        setRefreshToken(response.refreshToken);
    }

    async function logout() {
        if (refreshToken) {
            await logoutApi(refreshToken);
        }

        setUser(null);
        setAccessToken(null);
        setRefreshToken(null);
    }

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
