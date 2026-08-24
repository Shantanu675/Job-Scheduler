import {
    useCallback,
    useEffect,
    useState,
} from "react";
import {
    createQueue,
    getQueues,
    getQueueStats,
    pauseQueue,
    resumeQueue,
} from "../api/queues";
import { getProjects } from "../api/projects";
import { getRetryPolicies } from "../api/retryPolicies";

function Queues({ user }) {
    const [projects, setProjects] = useState([]);
    const [queues, setQueues] = useState([]);
    const [retryPolicies, setRetryPolicies] =
        useState([]);

    const [selectedProject, setSelectedProject] =
        useState("");

    const [showForm, setShowForm] = useState(false);

    const [name, setName] = useState("");
    const [priority, setPriority] = useState(0);
    const [maxConcurrency, setMaxConcurrency] =
        useState(10);
    const [retryPolicyId, setRetryPolicyId] =
        useState("");

    const [stats, setStats] = useState({});

    const [loading, setLoading] = useState(true);
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

    const loadQueues = useCallback(
        async (projectId) => {
            if (!projectId) {
                return;
            }

            const response =
                await getQueues(projectId);

            setQueues(response.data || []);
        },
        []
    );

    const loadRetryPolicies = useCallback(
        async (projectId) => {
            if (!projectId) {
                return;
            }

            const response =
                await getRetryPolicies(projectId);

            setRetryPolicies(response.data || []);
        },
        []
    );

    const refreshStats = useCallback(
        async (queueId) => {
            try {
                const response =
                    await getQueueStats(queueId);

                setStats((current) => ({
                    ...current,
                    [queueId]: response.data,
                }));
            } catch {
                // Keep existing UI state.
            }
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

                await Promise.all([
                    loadQueues(selectedProject),
                    loadRetryPolicies(selectedProject),
                ]);
            } catch (err) {
                setError(
                    err?.response?.data?.message ||
                    "Failed to load queues."
                );
            }
        };

        run();
    }, [
        selectedProject,
        loadQueues,
        loadRetryPolicies,
    ]);

    useEffect(() => {
        if (queues.length === 0) {
            return;
        }

        const run = async () => {
            await Promise.all(
                queues.map((queue) =>
                    refreshStats(queue.id)
                )
            );
        };

        run();
    }, [queues, refreshStats]);

    const handleCreate = async (event) => {
        event.preventDefault();

        try {
            setError("");
            setMessage("");

            await createQueue({
                projectId: Number(selectedProject),
                name,
                priority: Number(priority),
                maxConcurrency: Number(maxConcurrency),
                retryPolicyId: retryPolicyId
                    ? Number(retryPolicyId)
                    : null,
            });

            setName("");
            setPriority(0);
            setMaxConcurrency(10);
            setRetryPolicyId("");
            setShowForm(false);

            setMessage(
                "Queue created successfully."
            );

            await loadQueues(selectedProject);
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                "Failed to create queue."
            );
        }
    };

    const handleTogglePause = async (queue) => {
        try {
            setError("");
            setMessage("");

            if (queue.paused) {
                await resumeQueue(queue.id);
            } else {
                await pauseQueue(queue.id);
            }

            await loadQueues(selectedProject);

            setMessage(
                queue.paused
                    ? `${queue.name} resumed.`
                    : `${queue.name} paused.`
            );
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                "Queue action failed."
            );
        }
    };

    if (loading) {
        return (
            <div className="page-card">
                <div className="empty">
                    Loading queues...
                </div>
            </div>
        );
    }

    return (
        <div>
            <div className="page-header">
                <div>
                    <h2>Queues</h2>

                    <p className="page-description">
                        Configure priority, concurrency and retry
                        behavior.
                    </p>
                </div>

                <button
                    className="primary-button"
                    onClick={() =>
                        setShowForm(!showForm)
                    }
                >
                    {showForm ? "Cancel" : "+ New Queue"}
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
                    <h3>Create Queue</h3>

                    <label>Queue Name</label>

                    <input
                        className="form-input"
                        value={name}
                        onChange={(event) =>
                            setName(event.target.value)
                        }
                        required
                        maxLength={100}
                        placeholder="e.g. email-processing"
                    />

                    <label>Priority</label>

                    <input
                        className="form-input"
                        type="number"
                        value={priority}
                        onChange={(event) =>
                            setPriority(event.target.value)
                        }
                    />

                    <label>Max Concurrency</label>

                    <input
                        className="form-input"
                        type="number"
                        min="1"
                        value={maxConcurrency}
                        onChange={(event) =>
                            setMaxConcurrency(
                                event.target.value
                            )
                        }
                        required
                    />

                    <label>Retry Policy</label>

                    <select
                        className="form-input"
                        value={retryPolicyId}
                        onChange={(event) =>
                            setRetryPolicyId(
                                event.target.value
                            )
                        }
                    >
                        <option value="">
                            No retry policy
                        </option>

                        {retryPolicies.map((policy) => (
                            <option
                                key={policy.id}
                                value={policy.id}
                            >
                                {policy.name} ·{" "}
                                {policy.backoffType}
                            </option>
                        ))}
                    </select>

                    <button className="primary-button">
                        Create Queue
                    </button>
                </form>
            )}

            <div className="queue-grid">
                {queues.length === 0 ? (
                    <div className="page-card">
                        <div className="empty">
                            No queues found.
                        </div>
                    </div>
                ) : (
                    queues.map((queue) => {
                        const queueStats =
                            stats[queue.id];

                        return (
                            <div
                                className="queue-card"
                                key={queue.id}
                            >
                                <div className="queue-header">
                                    <div>
                                        <h3>{queue.name}</h3>

                                        <span className="item-muted">
                      Queue #{queue.id}
                    </span>
                                    </div>

                                    <span
                                        className={
                                            queue.paused
                                                ? "badge badge-yellow"
                                                : "badge badge-green"
                                        }
                                    >
                    {queue.paused
                        ? "PAUSED"
                        : "ACTIVE"}
                  </span>
                                </div>

                                <div className="queue-meta">
                                    <div>
                                        <span>Priority</span>
                                        <strong>
                                            {queue.priority}
                                        </strong>
                                    </div>

                                    <div>
                                        <span>Concurrency</span>
                                        <strong>
                                            {queue.maxConcurrency}
                                        </strong>
                                    </div>

                                    <div>
                                        <span>Retry Policy</span>
                                        <strong>
                                            {queue.retryPolicyId
                                                ? `#${queue.retryPolicyId}`
                                                : "None"}
                                        </strong>
                                    </div>
                                </div>

                                {queueStats && (
                                    <div className="queue-stats">
                                        <div>
                                            <span>Total</span>
                                            <strong>
                                                {queueStats.total}
                                            </strong>
                                        </div>

                                        <div>
                                            <span>Success</span>
                                            <strong className="text-green">
                                                {queueStats.success}
                                            </strong>
                                        </div>

                                        <div>
                                            <span>Failed</span>
                                            <strong className="text-red">
                                                {queueStats.failed}
                                            </strong>
                                        </div>
                                    </div>
                                )}

                                <div className="action-row">
                                    <button
                                        className="secondary-button"
                                        onClick={() =>
                                            handleTogglePause(queue)
                                        }
                                    >
                                        {queue.paused
                                            ? "Resume"
                                            : "Pause"}
                                    </button>
                                </div>
                            </div>
                        );
                    })
                )}
            </div>
        </div>
    );
}

export default Queues;