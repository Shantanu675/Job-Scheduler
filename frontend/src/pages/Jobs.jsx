import {
  useCallback,
  useEffect,
  useState,
} from "react";
import {
  createJob,
  getJob,
  getJobs,
  getJobExecutions,
} from "../api/jobs";
import { getProjects } from "../api/projects";
import { getQueues } from "../api/queues";

function Jobs({ user }) {
  const [projects, setProjects] = useState([]);
  const [queues, setQueues] = useState([]);
  const [jobs, setJobs] = useState([]);
  const [executions, setExecutions] =
      useState([]);

  const [selectedProject, setSelectedProject] =
      useState("");
  const [selectedQueue, setSelectedQueue] =
      useState("");

  const [showForm, setShowForm] = useState(false);
  const [selectedJob, setSelectedJob] =
      useState(null);

  const [jobType, setJobType] = useState("");
  const [payload, setPayload] = useState("{}");
  const [priority, setPriority] = useState(0);
  const [maxRetries, setMaxRetries] =
      useState(3);

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [detailsLoading, setDetailsLoading] =
      useState(false);
  const [executionsLoading, setExecutionsLoading] =
      useState(false);

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

        const data = response.data || [];

        setQueues(data);

        if (data.length > 0) {
          setSelectedQueue(String(data[0].id));
        } else {
          setSelectedQueue("");
        }
      },
      []
  );

  const loadJobs = useCallback(
      async (queueId) => {
        if (!queueId) {
          return;
        }

        const response =
            await getJobs(queueId);

        setJobs(response.data || []);
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
        await loadQueues(selectedProject);
      } catch (err) {
        setError(
            err?.response?.data?.message ||
            "Failed to load queues."
        );
      }
    };

    run();
  }, [selectedProject, loadQueues]);

  useEffect(() => {
    if (!selectedQueue) {
      return;
    }

    const run = async () => {
      try {
        setError("");
        await loadJobs(selectedQueue);
      } catch (err) {
        setError(
            err?.response?.data?.message ||
            "Failed to load jobs."
        );
      }
    };

    run();
  }, [selectedQueue, loadJobs]);

  const handleCreateJob = async (event) => {
    event.preventDefault();

    try {
      setSaving(true);
      setError("");
      setMessage("");

      let parsedPayload;

      try {
        parsedPayload = JSON.parse(payload);
      } catch {
        setError(
            "Payload must contain valid JSON."
        );
        return;
      }

      await createJob({
        projectId: Number(selectedProject),
        queueId: Number(selectedQueue),
        jobType,
        payload: JSON.stringify(
            parsedPayload
        ),
        priority: Number(priority),
        maxRetries: Number(maxRetries),
      });

      setJobType("");
      setPayload("{}");
      setPriority(0);
      setMaxRetries(3);
      setShowForm(false);

      setMessage(
          "Job created successfully."
      );

      await loadJobs(selectedQueue);
    } catch (err) {
      setError(
          err?.response?.data?.message ||
          "Failed to create job."
      );
    } finally {
      setSaving(false);
    }
  };

  const handleViewJob = async (jobId) => {
    try {
      setDetailsLoading(true);
      setExecutionsLoading(true);
      setError("");

      const [
        jobResponse,
        executionsResponse,
      ] = await Promise.all([
        getJob(jobId),
        getJobExecutions(jobId),
      ]);

      setSelectedJob(jobResponse.data);
      setExecutions(
          executionsResponse.data || []
      );
    } catch (err) {
      setError(
          err?.response?.data?.message ||
          "Failed to load job details."
      );
    } finally {
      setDetailsLoading(false);
      setExecutionsLoading(false);
    }
  };

  const getStatusClass = (status) => {
    switch (status) {
      case "SUCCESS":
        return "badge badge-green";

      case "FAILED":
        return "badge badge-red";

      case "PENDING":
      case "RETRYING":
        return "badge badge-yellow";

      case "RUNNING":
      case "CLAIMED":
        return "badge badge-blue";

      case "CANCELLED":
        return "badge badge-red";

      default:
        return "badge badge-blue";
    }
  };

  if (loading) {
    return (
        <div className="page-card">
          <div className="empty">
            Loading jobs...
          </div>
        </div>
    );
  }

  return (
      <div>
        <div className="page-header">
          <div>
            <h2>Jobs</h2>

            <p className="page-description">
              Create, monitor and inspect background
              jobs.
            </p>
          </div>

          <button
              className="primary-button"
              onClick={() => {
                setShowForm(!showForm);
                setMessage("");
                setError("");
              }}
              disabled={
                  !selectedProject ||
                  !selectedQueue
              }
          >
            {showForm
                ? "Cancel"
                : "+ Create Job"}
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
          <div className="selector-grid">
            <div>
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

            <div>
              <label>Queue</label>

              <select
                  className="form-input"
                  value={selectedQueue}
                  onChange={(event) =>
                      setSelectedQueue(
                          event.target.value
                      )
                  }
                  disabled={!selectedProject}
              >
                <option value="">
                  Select queue
                </option>

                {queues.map((queue) => (
                    <option
                        key={queue.id}
                        value={queue.id}
                    >
                      {queue.name}
                    </option>
                ))}
              </select>
            </div>
          </div>
        </div>

        {showForm && (
            <form
                className="page-card form-card"
                onSubmit={handleCreateJob}
            >
              <h3>Create Job</h3>

              <label>Job Type</label>

              <input
                  className="form-input"
                  value={jobType}
                  onChange={(event) =>
                      setJobType(event.target.value)
                  }
                  placeholder="e.g. EMAIL_NOTIFICATION"
                  maxLength={100}
                  required
              />

              <label>Payload</label>

              <textarea
                  className="form-input payload-input"
                  value={payload}
                  onChange={(event) =>
                      setPayload(event.target.value)
                  }
                  rows={8}
                  placeholder='{"message":"Hello"}'
                  required
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
              />

              <div className="job-form-actions">
                <button
                    className="primary-button"
                    type="submit"
                    disabled={saving}
                >
                  {saving
                      ? "Creating..."
                      : "Create Job"}
                </button>

                <button
                    className="secondary-button"
                    type="button"
                    onClick={() =>
                        setShowForm(false)
                    }
                >
                  Cancel
                </button>
              </div>
            </form>
        )}

        <div className="job-layout">
          <div className="page-card">
            <div className="section-title-row">
              <h3>Jobs</h3>

              <span className="badge badge-blue">
              {jobs.length} jobs
            </span>
            </div>

            {!selectedQueue ? (
                <div className="empty">
                  Select a queue to view jobs.
                </div>
            ) : jobs.length === 0 ? (
                <div className="empty">
                  No jobs found in this queue.
                </div>
            ) : (
                <div className="job-table">
                  <div className="job-table-header">
                    <span>ID</span>
                    <span>Type</span>
                    <span>Priority</span>
                    <span>Status</span>
                    <span>Retries</span>
                    <span></span>
                  </div>

                  {jobs.map((job) => (
                      <div
                          className="job-table-row"
                          key={job.id}
                      >
                  <span className="job-id">
                    #{job.id}
                  </span>

                        <span>
                    <strong>{job.jobType}</strong>
                  </span>

                        <span>{job.priority}</span>

                        <span>
                    <span
                        className={getStatusClass(
                            job.status
                        )}
                    >
                      {job.status}
                    </span>
                  </span>

                        <span>
                    {job.retryCount}/
                          {job.maxRetries}
                  </span>

                        <span>
                    <button
                        className="small-button"
                        onClick={() =>
                            handleViewJob(job.id)
                        }
                    >
                      View
                    </button>
                  </span>
                      </div>
                  ))}
                </div>
            )}
          </div>

          <div className="page-card job-details">
            <h3>Job Details</h3>

            {detailsLoading ? (
                <div className="empty">
                  Loading job...
                </div>
            ) : !selectedJob ? (
                <div className="empty">
                  Select a job to view details.
                </div>
            ) : (
                <div>
                  <div className="job-detail-title">
                    <div>
                      <h2>
                        #{selectedJob.id}
                      </h2>

                      <p>
                        {selectedJob.jobType}
                      </p>
                    </div>

                    <span
                        className={getStatusClass(
                            selectedJob.status
                        )}
                    >
                  {selectedJob.status}
                </span>
                  </div>

                  <div className="detail-grid">
                    <Detail
                        label="Project"
                        value={
                          selectedJob.projectId
                        }
                    />

                    <Detail
                        label="Queue"
                        value={
                          selectedJob.queueId
                        }
                    />

                    <Detail
                        label="Worker"
                        value={
                            selectedJob.workerId ??
                            "Not assigned"
                        }
                    />

                    <Detail
                        label="Priority"
                        value={
                          selectedJob.priority
                        }
                    />

                    <Detail
                        label="Retry Count"
                        value={`${selectedJob.retryCount}/${selectedJob.maxRetries}`}
                    />

                    <Detail
                        label="Retry Policy"
                        value={
                            selectedJob.retryPolicyId ??
                            "None"
                        }
                    />
                  </div>

                  <div className="detail-section">
                    <h4>Payload</h4>

                    <pre className="payload-view">
                  {selectedJob.payload}
                </pre>
                  </div>

                  <div className="detail-section">
                    <div className="section-title-row">
                      <h4>
                        Execution History
                      </h4>

                      <span className="badge badge-blue">
                    {executions.length} attempts
                  </span>
                    </div>

                    {executionsLoading ? (
                        <div className="empty">
                          Loading execution history...
                        </div>
                    ) : executions.length === 0 ? (
                        <div className="empty">
                          No execution history yet.
                        </div>
                    ) : (
                        <div className="execution-list">
                          {executions.map(
                              (execution) => (
                                  <div
                                      className="execution-card"
                                      key={execution.id}
                                  >
                                    <div className="execution-header">
                                      <div>
                                        <strong>
                                          Attempt{" "}
                                          {
                                            execution.attemptNumber
                                          }
                                        </strong>

                                        <div className="item-muted">
                                          Worker:{" "}
                                          {execution.workerId ??
                                              "Not assigned"}
                                        </div>
                                      </div>

                                      <span
                                          className={
                                            execution.status ===
                                            "SUCCESS"
                                                ? "badge badge-green"
                                                : execution.status ===
                                                "FAILED"
                                                    ? "badge badge-red"
                                                    : "badge badge-blue"
                                          }
                                      >
                              {execution.status}
                            </span>
                                    </div>

                                    <div className="execution-meta">
                                      <div>
                              <span>
                                Started
                              </span>

                                        <strong>
                                          {execution.startedAt
                                              ? new Date(
                                                  execution.startedAt
                                              ).toLocaleString()
                                              : "—"}
                                        </strong>
                                      </div>

                                      <div>
                              <span>
                                Completed
                              </span>

                                        <strong>
                                          {execution.completedAt
                                              ? new Date(
                                                  execution.completedAt
                                              ).toLocaleString()
                                              : "—"}
                                        </strong>
                                      </div>

                                      <div>
                              <span>
                                Duration
                              </span>

                                        <strong>
                                          {execution.durationMs !=
                                          null
                                              ? `${execution.durationMs} ms`
                                              : "—"}
                                        </strong>
                                      </div>
                                    </div>

                                    {execution.errorMessage && (
                                        <div className="execution-error">
                                          <strong>
                                            Error
                                          </strong>

                                          <p>
                                            {
                                              execution.errorMessage
                                            }
                                          </p>
                                        </div>
                                    )}
                                  </div>
                              )
                          )}
                        </div>
                    )}
                  </div>

                  <div className="detail-section">
                    <h4>Timeline</h4>

                    <TimelineRow
                        label="Created"
                        value={
                          selectedJob.createdAt
                        }
                    />

                    <TimelineRow
                        label="Available"
                        value={
                          selectedJob.availableAt
                        }
                    />

                    <TimelineRow
                        label="Claimed"
                        value={
                          selectedJob.claimedAt
                        }
                    />

                    <TimelineRow
                        label="Started"
                        value={
                          selectedJob.startedAt
                        }
                    />

                    <TimelineRow
                        label="Completed"
                        value={
                          selectedJob.completedAt
                        }
                    />
                  </div>
                </div>
            )}
          </div>
        </div>
      </div>
  );
}

function Detail({ label, value }) {
  return (
      <div className="detail-box">
        <span>{label}</span>
        <strong>{value}</strong>
      </div>
  );
}

function TimelineRow({ label, value }) {
  return (
      <div className="timeline-row">
        <span>{label}</span>

        <strong>
          {value
              ? new Date(value).toLocaleString()
              : "—"}
        </strong>
      </div>
  );
}

export default Jobs;