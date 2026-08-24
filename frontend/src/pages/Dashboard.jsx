import { useEffect, useState } from "react";
import { getProjects } from "../api/projects";
import { getQueues, getQueueStats } from "../api/queues";
import { getWorkers } from "../api/workers";
import { getDlq } from "../api/dlq";

function Dashboard({ user }) {
    const [projects, setProjects] = useState([]);
    const [queues, setQueues] = useState([]);
    const [workers, setWorkers] = useState([]);
    const [dlq, setDlq] = useState([]);
    const [stats, setStats] = useState({
        total: 0,
        pending: 0,
        success: 0,
        failed: 0,
    });
    const [error, setError] = useState("");

    useEffect(() => {
        const load = async () => {
            try {
                setError("");

                const projectResponse =
                    await getProjects(user.organizationId);

                const projectData = projectResponse.data || [];
                setProjects(projectData);

                let queueData = [];

                if (projectData.length > 0) {
                    const queueResponse =
                        await getQueues(projectData[0].id);

                    queueData = queueResponse.data || [];
                    setQueues(queueData);

                    if (queueData.length > 0) {
                        const statsResponse =
                            await getQueueStats(queueData[0].id);

                        setStats(statsResponse.data);
                    }
                }

                const [workerResponse, dlqResponse] =
                    await Promise.all([
                        getWorkers(),
                        getDlq(),
                    ]);

                setWorkers(workerResponse.data || []);
                setDlq(dlqResponse.data || []);

            } catch (err) {
                setError(
                    err?.response?.data?.message ||
                    "Could not load dashboard data."
                );
            }
        };

        load();
    }, [user.organizationId]);

    const onlineWorkers =
        workers.filter(
            (worker) => worker.status === "ONLINE"
        ).length;

    return (
        <div>
            {error && (
                <div className="error-banner">
                    {error}
                </div>
            )}

            <section className="hero">
                <h2>Good to see you, {user.name} 👋</h2>
                <p>
                    Monitor your distributed scheduler from one place.
                </p>
            </section>

            <section className="stats-grid">
                <Stat
                    title="Projects"
                    value={projects.length}
                    className="stat-indigo"
                />

                <Stat
                    title="Queues"
                    value={queues.length}
                    className="stat-cyan"
                />

                <Stat
                    title="Online Workers"
                    value={onlineWorkers}
                    className="stat-emerald"
                />

                <Stat
                    title="DLQ Jobs"
                    value={dlq.length}
                    className="stat-rose"
                />
            </section>

            <section className="stats-grid">
                <Stat
                    title="Total Jobs"
                    value={stats.total}
                    className="stat-indigo"
                />

                <Stat
                    title="Pending"
                    value={stats.pending}
                    className="stat-cyan"
                />

                <Stat
                    title="Successful"
                    value={stats.success}
                    className="stat-emerald"
                />

                <Stat
                    title="Failed"
                    value={stats.failed}
                    className="stat-rose"
                />
            </section>

            <section className="panel-grid">
                <div className="panel">
                    <h3>Projects</h3>

                    {projects.length === 0 ? (
                        <div className="empty">
                            No projects yet.
                        </div>
                    ) : (
                        <div className="list">
                            {projects.map((project) => (
                                <div
                                    className="list-item"
                                    key={project.id}
                                >
                                    <div>
                                        <strong>{project.name}</strong>
                                        <div className="item-muted">
                                            {project.description ||
                                                "No description"}
                                        </div>
                                    </div>

                                    <span className="badge badge-blue">
                    #{project.id}
                  </span>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                <div className="panel">
                    <h3>Queues</h3>

                    {queues.length === 0 ? (
                        <div className="empty">
                            No queues yet.
                        </div>
                    ) : (
                        <div className="list">
                            {queues.map((queue) => (
                                <div
                                    className="list-item"
                                    key={queue.id}
                                >
                                    <div>
                                        <strong>{queue.name}</strong>

                                        <div className="item-muted">
                                            Priority {queue.priority} ·
                                            Concurrency {queue.maxConcurrency}
                                        </div>
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
                            ))}
                        </div>
                    )}
                </div>

                <div className="panel">
                    <h3>Workers</h3>

                    <div className="list">
                        {workers.map((worker) => (
                            <div
                                className="list-item"
                                key={worker.id}
                            >
                                <div>
                                    <strong>
                                        {worker.workerId}
                                    </strong>

                                    <div className="item-muted">
                                        {worker.hostname}
                                    </div>
                                </div>

                                <span
                                    className={
                                        worker.status === "ONLINE"
                                            ? "badge badge-green"
                                            : "badge badge-red"
                                    }
                                >
                  {worker.status}
                </span>
                            </div>
                        ))}
                    </div>
                </div>

                <div className="panel">
                    <h3>Dead Letter Queue</h3>

                    {dlq.length === 0 ? (
                        <div className="empty">
                            🎉 No failed jobs in DLQ.
                        </div>
                    ) : (
                        <div className="list">
                            {dlq.slice(0, 6).map((entry) => (
                                <div
                                    className="list-item"
                                    key={entry.id}
                                >
                                    <div>
                                        <strong>
                                            Job #{entry.jobId}
                                        </strong>

                                        <div className="item-muted">
                                            {entry.reason}
                                        </div>
                                    </div>

                                    <span className="badge badge-red">
                    {entry.retryCount} attempts
                  </span>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </section>
        </div>
    );
}

function Stat({ title, value, className }) {
    return (
        <div className={`stat-card ${className}`}>
            <div className="label">{title}</div>
            <div className="value">{value}</div>
        </div>
    );
}

export default Dashboard;