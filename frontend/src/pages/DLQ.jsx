import {
  useCallback,
  useEffect,
  useState,
} from "react";
import {
  getDlq,
  requeueDlq,
} from "../api/dlq";

function DLQ() {
  const [entries, setEntries] = useState([]);
  const [selectedEntry, setSelectedEntry] =
      useState(null);

  const [loading, setLoading] = useState(true);
  const [requeueingId, setRequeueingId] =
      useState(null);

  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const loadDlq = useCallback(async () => {
    try {
      setError("");

      const response = await getDlq();

      setEntries(response.data || []);
    } catch (err) {
      setError(
          err?.response?.data?.message ||
          "Failed to load dead letter queue."
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const runInitialLoad = async () => {
      await loadDlq();
    };

    runInitialLoad();

    const interval = setInterval(() => {
      loadDlq();
    }, 5000);

    return () => clearInterval(interval);
  }, [loadDlq]);

  const handleRequeue = async (entry) => {
    const confirmed = window.confirm(
        `Requeue Job #${entry.jobId}?`
    );

    if (!confirmed) {
      return;
    }

    try {
      setRequeueingId(entry.id);
      setError("");
      setMessage("");

      await requeueDlq(entry.id);

      setSelectedEntry(null);

      setMessage(
          `Job #${entry.jobId} has been requeued successfully.`
      );

      await loadDlq();
    } catch (err) {
      setError(
          err?.response?.data?.message ||
          "Failed to requeue job."
      );
    } finally {
      setRequeueingId(null);
    }
  };

  const selectEntry = (entry) => {
    setSelectedEntry(entry);
    setMessage("");
    setError("");
  };

  return (
      <div>
        <div className="page-header">
          <div>
            <h2>Dead Letter Queue</h2>

            <p className="page-description">
              Review permanently failed jobs and send them
              back into the scheduler.
            </p>
          </div>

          <button
              className="secondary-button"
              onClick={loadDlq}
          >
            Refresh
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

        {!loading && (
            <div className="stats-grid">
              <div className="stat-card stat-rose">
                <div className="label">
                  DLQ Entries
                </div>

                <div className="value">
                  {entries.length}
                </div>
              </div>

              <div className="stat-card stat-indigo">
                <div className="label">
                  Total Attempts
                </div>

                <div className="value">
                  {entries.reduce(
                      (total, entry) =>
                          total + (entry.retryCount || 0),
                      0
                  )}
                </div>
              </div>

              <div className="stat-card stat-cyan">
                <div className="label">
                  Max Retry Failures
                </div>

                <div className="value">
                  {
                    entries.filter(
                        (entry) =>
                            entry.reason ===
                            "MAX_RETRIES_EXCEEDED"
                    ).length
                  }
                </div>
              </div>

              <div className="stat-card stat-emerald">
                <div className="label">
                  Auto Refresh
                </div>

                <div className="value">
                  5s
                </div>
              </div>
            </div>
        )}

        {loading ? (
            <div className="page-card">
              <div className="empty">
                Loading dead letter queue...
              </div>
            </div>
        ) : entries.length === 0 ? (
            <div className="page-card dlq-empty">
              <div className="dlq-empty-icon">
                ✓
              </div>

              <h3>
                Dead Letter Queue is empty
              </h3>

              <p>
                No permanently failed jobs require attention.
              </p>
            </div>
        ) : (
            <div className="dlq-layout">
              <div className="page-card">
                <div className="section-title-row">
                  <h3>Failed Jobs</h3>

                  <span className="badge badge-red">
                {entries.length}
              </span>
                </div>

                <div className="dlq-list">
                  {entries.map((entry) => (
                      <button
                          key={entry.id}
                          className={
                            selectedEntry?.id === entry.id
                                ? "dlq-item selected"
                                : "dlq-item"
                          }
                          onClick={() =>
                              selectEntry(entry)
                          }
                      >
                        <div className="dlq-item-main">
                          <strong>
                            Job #{entry.jobId}
                          </strong>

                          <span>
                      {entry.reason}
                    </span>
                        </div>

                        <div className="dlq-item-meta">
                    <span className="badge badge-red">
                      {entry.retryCount} attempts
                    </span>

                          <span className="dlq-time">
                      {new Date(
                          entry.movedAt
                      ).toLocaleString()}
                    </span>
                        </div>
                      </button>
                  ))}
                </div>
              </div>

              <div className="page-card dlq-details">
                <h3>DLQ Details</h3>

                {!selectedEntry ? (
                    <div className="empty">
                      Select a failed job to view details.
                    </div>
                ) : (
                    <>
                      <div className="dlq-detail-header">
                        <div className="dlq-job-icon">
                          !
                        </div>

                        <div>
                          <h2>
                            Job #{selectedEntry.jobId}
                          </h2>

                          <p>
                            Entry #{selectedEntry.id}
                          </p>
                        </div>
                      </div>

                      <div className="detail-grid">
                        <div className="detail-box">
                          <span>Reason</span>

                          <strong>
                            {selectedEntry.reason}
                          </strong>
                        </div>

                        <div className="detail-box">
                          <span>Retry Count</span>

                          <strong>
                            {selectedEntry.retryCount}
                          </strong>
                        </div>

                        <div className="detail-box">
                          <span>Moved At</span>

                          <strong>
                            {new Date(
                                selectedEntry.movedAt
                            ).toLocaleString()}
                          </strong>
                        </div>

                        <div className="detail-box">
                          <span>DLQ ID</span>

                          <strong>
                            #{selectedEntry.id}
                          </strong>
                        </div>
                      </div>

                      <div className="detail-section">
                        <h4>Final Error</h4>

                        <div className="dlq-error-box">
                          {selectedEntry.finalError ||
                              "No error message recorded."}
                        </div>
                      </div>

                      <div className="dlq-actions">
                        <button
                            className="primary-button"
                            onClick={() =>
                                handleRequeue(
                                    selectedEntry
                                )
                            }
                            disabled={
                                requeueingId ===
                                selectedEntry.id
                            }
                        >
                          {requeueingId ===
                          selectedEntry.id
                              ? "Requeueing..."
                              : "↻ Requeue Job"}
                        </button>
                      </div>

                      <div className="dlq-warning">
                        Requeueing resets the retry count and
                        places the job back into the normal
                        scheduler flow.
                      </div>
                    </>
                )}
              </div>
            </div>
        )}
      </div>
  );
}

export default DLQ;