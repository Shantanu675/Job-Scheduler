import { useState } from "react";
import { login } from "./api/auth";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import Projects from "./pages/Projects";
import Queues from "./pages/Queues";
import Jobs from "./pages/Jobs";
import Workers from "./pages/Workers";
import RetryPolicies from "./pages/RetryPolicies";
import DLQ from "./pages/DLQ";
import "./App.css";

function App() {
    const [page, setPage] = useState(
        localStorage.getItem("token")
            ? "dashboard"
            : "login"
    );

    const [activePage, setActivePage] =
        useState("dashboard");

    const [user, setUser] = useState(() => {
        const storedUser = localStorage.getItem("user");

        return storedUser
            ? JSON.parse(storedUser)
            : null;
    });

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleLogin = async (event) => {
        event.preventDefault();

        setError("");
        setLoading(true);

        try {
            const data = await login(email, password);

            const currentUser = {
                userId: data.userId,
                organizationId: data.organizationId,
                email: data.email,
                name: data.name,
                role: data.role,
            };

            localStorage.setItem("token", data.token);
            localStorage.setItem(
                "user",
                JSON.stringify(currentUser)
            );

            setUser(currentUser);
            setPage("dashboard");
            setActivePage("dashboard");

            setEmail("");
            setPassword("");
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                "Invalid email or password."
            );
        } finally {
            setLoading(false);
        }
    };

    const handleLogout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("user");

        setUser(null);
        setPage("login");
        setActivePage("dashboard");

        setEmail("");
        setPassword("");
    };

    const navigate = (targetPage) => {
        setActivePage(targetPage);
    };

    if (page === "register") {
        return (
            <Register
                onRegistered={() => setPage("login")}
                onLogin={() => setPage("login")}
            />
        );
    }

    if (page === "dashboard" && user) {
        return (
            <div className="app-shell">
                <aside className="sidebar">
                    <div className="brand">
                        <h2 className="brand-title">
                            Scheduler
                        </h2>

                        <p className="brand-subtitle">
                            Distributed Jobs Platform
                        </p>
                    </div>

                    <nav className="nav">
                        <button
                            className={
                                activePage === "dashboard"
                                    ? "nav-button active"
                                    : "nav-button"
                            }
                            onClick={() =>
                                navigate("dashboard")
                            }
                        >
                            Dashboard
                        </button>

                        <button
                            className={
                                activePage === "projects"
                                    ? "nav-button active"
                                    : "nav-button"
                            }
                            onClick={() =>
                                navigate("projects")
                            }
                        >
                            Projects
                        </button>

                        <button
                            className={
                                activePage === "queues"
                                    ? "nav-button active"
                                    : "nav-button"
                            }
                            onClick={() =>
                                navigate("queues")
                            }
                        >
                            Queues
                        </button>

                        <button
                            className={
                                activePage === "jobs"
                                    ? "nav-button active"
                                    : "nav-button"
                            }
                            onClick={() =>
                                navigate("jobs")
                            }
                        >
                            Jobs
                        </button>

                        <button
                            className={
                                activePage === "workers"
                                    ? "nav-button active"
                                    : "nav-button"
                            }
                            onClick={() =>
                                navigate("workers")
                            }
                        >
                            Workers
                        </button>

                        <button
                            className={
                                activePage === "retry-policies"
                                    ? "nav-button active"
                                    : "nav-button"
                            }
                            onClick={() =>
                                navigate("retry-policies")
                            }
                        >
                            Retry Policies
                        </button>

                        <button
                            className={
                                activePage === "dlq"
                                    ? "nav-button active"
                                    : "nav-button"
                            }
                            onClick={() => navigate("dlq")}
                        >
                            Dead Letter Queue
                        </button>
                    </nav>

                    <div className="sidebar-bottom">
                        <div className="user-mini">
                            <strong>{user.name}</strong>
                            <span>{user.email}</span>
                            <span>
                Organization{" "}
                                {user.organizationId}
              </span>
                        </div>

                        <button
                            className="logout-button"
                            onClick={handleLogout}
                        >
                            Logout
                        </button>
                    </div>
                </aside>

                <main className="main">
                    <header className="topbar">
                        <div>
                            <h1>
                                {activePage === "dashboard" &&
                                    "Dashboard"}
                                {activePage === "projects" &&
                                    "Projects"}
                                {activePage === "queues" &&
                                    "Queues"}
                                {activePage === "jobs" &&
                                    "Jobs"}
                                {activePage === "workers" &&
                                    "Workers"}
                                {activePage === "retry-policies" &&
                                    "Retry Policies"}
                                {activePage === "dlq" &&
                                    "Dead Letter Queue"}
                            </h1>

                            <p>
                                Distributed scheduler management
                            </p>
                        </div>

                        <div className="topbar-user">
                            <span>{user.name}</span>

                            <span className="badge badge-blue">
                {user.role}
              </span>
                        </div>
                    </header>

                    <div className="content">
                        {activePage === "dashboard" && (
                            <Dashboard user={user} />
                        )}

                        {activePage === "projects" && (
                            <Projects user={user} />
                        )}

                        {activePage === "queues" && (
                            <Queues user={user} />
                        )}

                        {activePage === "jobs" && (
                            <Jobs user={user} />
                        )}

                        {activePage === "workers" && (
                            <Workers />
                        )}

                        {activePage === "retry-policies" && (
                            <RetryPolicies user={user} />
                        )}

                        {activePage === "dlq" && <DLQ />}
                    </div>
                </main>
            </div>
        );
    }

    return (
        <div className="login-page">
            <form
                className="login-card"
                onSubmit={handleLogin}
            >
                <div className="login-brand">
                    <div className="login-logo">S</div>

                    <div>
                        <h1>Scheduler</h1>
                        <p>Distributed Job Platform</p>
                    </div>
                </div>

                <h2>Welcome back</h2>

                <p className="login-description">
                    Sign in to manage your distributed jobs.
                </p>

                <label className="login-label">
                    Email
                </label>

                <input
                    className="login-input"
                    type="email"
                    placeholder="you@example.com"
                    value={email}
                    onChange={(event) =>
                        setEmail(event.target.value)
                    }
                    required
                />

                <label className="login-label">
                    Password
                </label>

                <input
                    className="login-input"
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(event) =>
                        setPassword(event.target.value)
                    }
                    required
                />

                {error && (
                    <div className="error-banner">
                        {error}
                    </div>
                )}

                <button
                    className="primary-button login-submit"
                    type="submit"
                    disabled={loading}
                >
                    {loading ? "Signing in..." : "Sign In"}
                </button>

                <button
                    type="button"
                    className="secondary-button login-signup"
                    onClick={() => setPage("register")}
                >
                    Create an account
                </button>
            </form>
        </div>
    );
}

export default App;