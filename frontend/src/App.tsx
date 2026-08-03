import { Navigate, Route, Routes } from "react-router";
import { AppLayout } from "./components/AppLayout";
import { ProtectedRoute } from "./auth/ProtectedRoute";
import { DashboardPage } from "./pages/DashboardPage";
import { IntegrationsPage } from "./pages/IntegrationsPage";
import { LoginPage } from "./pages/LoginPage";

export default function App() {
    return (
        <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/login" element={<LoginPage />} />

            <Route element={<ProtectedRoute />}>
                <Route element={<AppLayout />}>
                    <Route path="/dashboard" element={<DashboardPage />} />
                    <Route path="/integrations" element={<IntegrationsPage />} />
                </Route>
            </Route>
        </Routes>
    );
}
