import {
    useCallback,
    useEffect,
    useState,
} from "react";
import { getProjects } from "../api/projects";
import {
    createRetryPolicy,
    getRetryPolicies,
} from "../api/retryPolicies";

function RetryPolicies({ user }) {
    const [projects, setProjects] = useState([]);
    const [policies, setPolicies] = useState([]);

    const [selectedProject, setSelectedProject] =
        useState("");

    const [showForm, setShowForm] = useState(false);

    const [name, setName] = useState("");
    const [maxRetries, setMaxRetries] =
        useState(3);
    const [backoffType, setBackoffType] =
        useState("EXPONENTIAL");
    const [initialDelayMs, setInitialDelayMs] =
        useState(2000);
    const [maxDelayMs, setMaxDelayMs] =
        useState(10000);

    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");
    const [message, setMessage] = useState("");

    const loadProjects = useCallback(async () => {
        const response =
            await getProjects(user.organizationId);

        const data = response.data || [];

        setProjects(data);

        if (data.length > 0) {
            setSelectedProject(String(data[0].id));
        } else {
            setSelectedProject("");
        }
    }, [user.organizationId]);

    const loadPolicies = useCallback(
        async (projectId) => {
            if (!projectId) {
                return;
            }

            const response =
                await getRetryPolicies(projectId);

            setPolicies(response.data || []);
        },
        []
    );

    useEffect(() => {
        const run = async () => {
            try {
                setLoading(true);
                setError("");

                await loadProjects();
            } catch (err) {
                setError(
                    err?.response?.data?.message ||
                    "Failed to load projects."
                );
            } finally {
                setLoading(false);
            }
        };

        run();
    }, [loadProjects]);

    useEffect(() => {
        if (!selectedProject) {
            return;
        }

        const run = async () => {
            try {
                setError("");
                await loadPolicies(selectedProject);
            } catch (err) {
                setError(
                    err?.response?.data?.message ||
                    "Failed to load retry policies."
                );
            }
        };

        run();
    }, [selectedProject, loadPolicies]);

    const handleCreate = async (event) => {
        event.preventDefault();

        try {
            setSaving(true);
            setError("");
            setMessage("");

            if (
                Number(initialDelayMs) >
                Number(maxDelayMs)
            ) {
                setError(
                    "Initial delay cannot exceed maximum delay."
                );
                return;
            }

            await createRetryPolicy({
                projectId: Number(selectedProject),
                name,
                maxRetries: Number(maxRetries),
                backoffType,
                initialDelayMs:
                    Number(initialDelayMs),
                maxDelayMs: Number(maxDelayMs),
            });

            setName("");
            setMaxRetries(3);
            setBackoffType("EXPONENTIAL");
            setInitialDelayMs(2000);
            setMaxDelayMs(10000);

            setShowForm(false);

            setMessage(
                "Retry policy created successfully."
            );

            await loadPolicies(selectedProject);
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                "Failed to create retry policy."
            );
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <div className="page-card">
                <div className="empty">
                    Loading retry policies...
                </div>
            </div>
        );
    }

    return (
        <div>
            <div className="page-header">
                <div>
                    <h2>Retry Policies</h2>

                    <p className="page-description">
                        Configure retry count and backoff behavior.
                    </p>
                </div>

                <button
                    className="primary-button"
                    onClick={() => {
                        setShowForm(!showForm);
                        setError("");
                        setMessage("");
                    }}
                >
                    {showForm
                        ? "Cancel"
                        : "+ New Policy"}
                </button>
            </div>

            {error && (
                <div className="error-banner">
                    {error}
                </div>
            )}

            {message && (
                <div className="success-banner">
                    {message}
                </div>
            )}

            <div className="page-card project-selector">
                <label>Project</label>

                <select
                    className="form-input"
                    value={selectedProject}
                    onChange={(event) =>
                        setSelectedProject(
                            event.target.value
                        )
                    }
                >
                    <option value="">
                        Select project
                    </option>

                    {projects.map((project) => (
                        <option
                            key={project.id}
                            value={project.id}
                        >
                            {project.name}
                        </option>
                    ))}
                </select>
            </div>

            {showForm && (
                <form
                    className="page-card form-card"
                    onSubmit={handleCreate}
                >
                    <h3>Create Retry Policy</h3>

                    <label>Name</label>

                    <input
                        className="form-input"
                        value={name}
                        onChange={(event) =>
                            setName(event.target.value)
                        }
                        placeholder="e.g. payment-retry"
                        required
                    />

                    <label>Maximum Retries</label>

                    <input
                        className="form-input"
                        type="number"
                        min="0"
                        value={maxRetries}
                        onChange={(event) =>
                            setMaxRetries(
                                event.target.value
                            )
                        }
                        required
                    />

                    <label>Backoff Type</label>

                    <select
                        className="form-input"
                        value={backoffType}
                        onChange={(event) =>
                            setBackoffType(
                                event.target.value
                            )
                        }
                    >
                        <option value="FIXED">FIXED</option>
                        <option value="LINEAR">LINEAR</option>
                        <option value="EXPONENTIAL">
                            EXPONENTIAL
                        </option>
                    </select>

                    <label>Initial Delay (ms)</label>

                    <input
                        className="form-input"
                        type="number"
                        min="0"
                        value={initialDelayMs}
                        onChange={(event) =>
                            setInitialDelayMs(
                                event.target.value
                            )
                        }
                        required
                    />

                    <label>Maximum Delay (ms)</label>

                    <input
                        className="form-input"
                        type="number"
                        min="0"
                        value={maxDelayMs}
                        onChange={(event) =>
                            setMaxDelayMs(
                                event.target.value
                            )
                        }
                        required
                    />

                    <button
                        className="primary-button"
                        type="submit"
                        disabled={saving}
                    >
                        {saving
                            ? "Creating..."
                            : "Create Policy"}
                    </button>
                </form>
            )}

            <div className="retry-policy-grid">
                {policies.length === 0 ? (
                    <div className="page-card">
                        <div className="empty">
                            No retry policies found.
                        </div>
                    </div>
                ) : (
                    policies.map((policy) => (
                        <div
                            className="retry-policy-card"
                            key={policy.id}
                        >
                            <div className="retry-policy-header">
                                <div className="retry-policy-icon">
                                    ↻
                                </div>

                                <span className="badge badge-blue">
                  {policy.backoffType}
                </span>
                            </div>

                            <h3>{policy.name}</h3>

                            <p className="item-muted">
                                Policy #{policy.id}
                            </p>

                            <div className="policy-metrics">
                                <div>
                                    <span>Max Retries</span>
                                    <strong>
                                        {policy.maxRetries}
                                    </strong>
                                </div>

                                <div>
                                    <span>Initial Delay</span>
                                    <strong>
                                        {policy.initialDelayMs} ms
                                    </strong>
                                </div>

                                <div>
                                    <span>Max Delay</span>
                                    <strong>
                                        {policy.maxDelayMs} ms
                                    </strong>
                                </div>
                            </div>

                            <div className="policy-flow">
                                {policy.backoffType ===
                                    "FIXED" &&
                                    "Every retry waits the same delay."}

                                {policy.backoffType ===
                                    "LINEAR" &&
                                    "Delay increases linearly."}

                                {policy.backoffType ===
                                    "EXPONENTIAL" &&
                                    "Delay grows exponentially between retries."}
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
}

export default RetryPolicies;