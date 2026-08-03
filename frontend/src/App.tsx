import { Navigate, Route, Routes } from "react-router";
import { AppLayout } from "./components/AppLayout";
import { ProtectedRoute } from "./auth/ProtectedRoute";
import { DashboardPage } from "./pages/DashboardPage";
import { IntegrationsPage } from "./pages/IntegrationsPage";
import { LoginPage } from "./pages/LoginPage";
import { SyncJobsPage } from "./pages/SyncJobsPage";
import { AuditLogsPage } from "./pages/AuditLogsPage";
import { WebhookEventsPage } from "./pages/WebhookEventsPage";
import { EntityMappingsPage } from "./pages/EntityMappingsPage";
import { ConflictRulesPage } from "./pages/ConflictRulesPage";

export default function App() {
    return (
        <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/login" element={<LoginPage />} />

            <Route element={<ProtectedRoute />}>
                <Route element={<AppLayout />}>
                    <Route path="/dashboard" element={<DashboardPage />} />
                    <Route path="/integrations" element={<IntegrationsPage />} />
                    <Route path="/sync-jobs" element={<SyncJobsPage />} />
                    <Route path="/audit-logs" element={<AuditLogsPage />} />
                    <Route path="/webhook-events" element={<WebhookEventsPage />} />
                    <Route path="/entity-mappings" element={<EntityMappingsPage />} />
                    <Route path="/conflict-rules" element={<ConflictRulesPage />} />
                </Route>
            </Route>
        </Routes>
    );
}
