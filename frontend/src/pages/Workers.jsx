import {
    useCallback,
    useEffect,
    useState,
} from "react";
import { getWorkers } from "../api/workers";

function Workers() {
    const [workers, setWorkers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] =
        useState(false);
    const [error, setError] = useState("");

    const loadWorkers = useCallback(
        async (showLoader = false) => {
            try {
                if (showLoader) {
                    setRefreshing(true);
                }

                setError("");

                const response = await getWorkers();

                setWorkers(response.data || []);
            } catch (err) {
                setError(
                    err?.response?.data?.message ||
                    "Failed to load workers."
                );
            } finally {
                setLoading(false);
                setRefreshing(false);
            }
        },
        []
    );

    useEffect(() => {
        const runInitialLoad = async () => {
            await loadWorkers(true);
        };

        runInitialLoad();

        const interval = setInterval(() => {
            loadWorkers(false);
        }, 5000);

        return () => clearInterval(interval);
    }, [loadWorkers]);

    const onlineWorkers = workers.filter(
        (worker) => worker.status === "ONLINE"
    );

    const offlineWorkers = workers.filter(
        (worker) => worker.status !== "ONLINE"
    );

    const formatTimestamp = (timestamp) => {
        if (!timestamp) {
            return "Never";
        }

        return new Date(timestamp).toLocaleString();
    };

    const getStatusClass = (status) =>
        status === "ONLINE"
            ? "badge badge-green"
            : "badge badge-red";

    return (
        <div>
            <div className="page-header">
                <div>
                    <h2>Workers</h2>

                    <p className="page-description">
                        Monitor worker health, status and heartbeat
                        activity.
                    </p>
                </div>

                <button
                    className="secondary-button"
                    onClick={() => loadWorkers(true)}
                    disabled={refreshing}
                >
                    {refreshing
                        ? "Refreshing..."
                        : "Refresh"}
                </button>
            </div>

            {error && (
                <div className="error-banner">
                    {error}
                </div>
            )}

            {loading ? (
                <div className="page-card">
                    <div className="empty">
                        Loading workers...
                    </div>
                </div>
            ) : (
                <>
                    <div className="stats-grid">
                        <div className="stat-card stat-indigo">
                            <div className="label">
                                Total Workers
                            </div>

                            <div className="value">
                                {workers.length}
                            </div>
                        </div>

                        <div className="stat-card stat-emerald">
                            <div className="label">
                                Online
                            </div>

                            <div className="value">
                                {onlineWorkers.length}
                            </div>
                        </div>

                        <div className="stat-card stat-rose">
                            <div className="label">
                                Offline
                            </div>

                            <div className="value">
                                {offlineWorkers.length}
                            </div>
                        </div>

                        <div className="stat-card stat-cyan">
                            <div className="label">
                                Refresh Interval
                            </div>

                            <div className="value">
                                5s
                            </div>
                        </div>
                    </div>

                    {workers.length === 0 ? (
                        <div className="page-card">
                            <div className="empty">
                                No workers registered.
                            </div>
                        </div>
                    ) : (
                        <div className="worker-grid">
                            {workers.map((worker) => (
                                <div
                                    className="worker-card"
                                    key={worker.id}
                                >
                                    <div className="worker-header">
                                        <div className="worker-icon">
                                            W
                                        </div>

                                        <span
                                            className={getStatusClass(
                                                worker.status
                                            )}
                                        >
                      {worker.status}
                    </span>
                                    </div>

                                    <h3>{worker.workerId}</h3>

                                    <p className="item-muted">
                                        {worker.hostname}
                                    </p>

                                    <div className="worker-info">
                                        <div>
                                            <span>Database ID</span>
                                            <strong>{worker.id}</strong>
                                        </div>

                                        <div>
                                            <span>Last Heartbeat</span>
                                            <strong>
                                                {formatTimestamp(
                                                    worker.lastHeartbeatAt
                                                )}
                                            </strong>
                                        </div>

                                        <div>
                                            <span>Created</span>
                                            <strong>
                                                {formatTimestamp(
                                                    worker.createdAt
                                                )}
                                            </strong>
                                        </div>

                                        <div>
                                            <span>Updated</span>
                                            <strong>
                                                {formatTimestamp(
                                                    worker.updatedAt
                                                )}
                                            </strong>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </>
            )}
        </div>
    );
}

export default Workers;