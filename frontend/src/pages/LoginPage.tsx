import { type FormEvent, useState } from "react";
import { useNavigate } from "react-router";
import { useAuth } from "../auth/AuthContext";

export function LoginPage() {
    const navigate = useNavigate();
    const { login } = useAuth();

    const [email, setEmail] = useState("admin@acme.com");
    const [password, setPassword] = useState("Admin@12345");
    const [error, setError] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        setError(null);
        setIsLoading(true);

        try {
            await login(email, password);
            navigate("/dashboard");
        } catch (exception) {
            setError("Invalid email or password");
        } finally {
            setIsLoading(false);
        }
    }

    return (
        <main className="login-page">
            <section className="login-card">
                <p className="eyebrow">SyncForge</p>
                <h1>Sign in</h1>
                <p className="muted">
                    Login to monitor tenants, integrations, sync jobs, and audit logs.
                </p>

                <form onSubmit={handleSubmit}>
                    <label>
                        Email
                        <input
                            value={email}
                            onChange={(event) => setEmail(event.target.value)}
                            type="email"
                            placeholder="admin@acme.com"
                        />
                    </label>

                    <label>
                        Password
                        <input
                            value={password}
                            onChange={(event) => setPassword(event.target.value)}
                            type="password"
                            placeholder="Password"
                        />
                    </label>

                    {error && <p className="error">{error}</p>}

                    <button type="submit" disabled={isLoading}>
                        {isLoading ? "Signing in..." : "Sign in"}
                    </button>
                </form>
            </section>
        </main>
    );
}
