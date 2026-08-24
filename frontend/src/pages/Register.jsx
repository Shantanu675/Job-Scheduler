import { useState } from "react";
import { register } from "../api/auth";

function Register({ onRegistered, onLogin }) {
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (event) => {
        event.preventDefault();

        setMessage("");
        setError("");
        setLoading(true);

        try {
            await register(name, email, password);

            setMessage(
                "Account created successfully. You can now sign in."
            );

            setName("");
            setEmail("");
            setPassword("");

            if (onRegistered) {
                onRegistered();
            }
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                "Registration failed."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={styles.page}>
            <form style={styles.card} onSubmit={handleSubmit}>
                <h1>Create Account</h1>

                <label>Name</label>
                <input
                    style={styles.input}
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    required
                />

                <label>Email</label>
                <input
                    style={styles.input}
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                />

                <label>Password</label>
                <input
                    style={styles.input}
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    minLength={6}
                    required
                />

                {message && <p style={styles.success}>{message}</p>}
                {error && <p style={styles.error}>{error}</p>}

                <button style={styles.button} disabled={loading}>
                    {loading ? "Creating..." : "Create Account"}
                </button>

                <button
                    type="button"
                    style={styles.linkButton}
                    onClick={onLogin}
                >
                    Back to Login
                </button>
            </form>
        </div>
    );
}

const styles = {
    page: {
        minHeight: "100vh",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        background: "#f4f6f8",
    },
    card: {
        width: "400px",
        padding: "32px",
        background: "white",
        borderRadius: "12px",
        boxShadow: "0 10px 30px rgba(0,0,0,0.08)",
    },
    input: {
        width: "100%",
        boxSizing: "border-box",
        padding: "11px",
        margin: "8px 0 16px",
        border: "1px solid #d0d5dd",
        borderRadius: "8px",
    },
    button: {
        width: "100%",
        padding: "12px",
        background: "#111827",
        color: "white",
        border: "none",
        borderRadius: "8px",
    },
    linkButton: {
        width: "100%",
        marginTop: "12px",
        padding: "10px",
        background: "transparent",
        border: "none",
        cursor: "pointer",
    },
    success: {
        color: "green",
    },
    error: {
        color: "red",
    },
};

export default Register;