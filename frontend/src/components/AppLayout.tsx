import { NavLink, Outlet } from "react-router";
import { useAuth } from "../auth/AuthContext";

export function AppLayout() {
    const { user, logout } = useAuth();

    return (
        <div className="app-shell">
            <aside className="sidebar">
                <div>
                    <p className="eyebrow">SyncForge</p>
                    <p className="sidebar-user">{user?.email}</p>
                    <p className={`sidebar-role role-${user?.role?.toLowerCase()}`}>
                        {user?.role}
                    </p>
                </div>

                <nav className="nav">
                    <NavLink to="/dashboard">Dashboard</NavLink>
                    <NavLink to="/integrations">Integrations</NavLink>
                    <NavLink to="/webhook-events">Webhook Events</NavLink>
                    <NavLink to="/sync-jobs">Sync Jobs</NavLink>
                    <NavLink to="/audit-logs">Audit Logs</NavLink>
                    <NavLink to="/entity-mappings">Entity Mappings</NavLink>
                    <NavLink to="/conflict-rules">Conflict Rules</NavLink>
                </nav>

                <button onClick={logout}>Logout</button>
            </aside>

            <div className="content">
                <Outlet/>
            </div>
        </div>
    );
}
