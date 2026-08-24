import {
    useCallback,
    useEffect,
    useState,
} from "react";
import {
    createProject,
    getProjects,
} from "../api/projects";

function Projects({ user }) {
    const [projects, setProjects] = useState([]);
    const [showForm, setShowForm] = useState(false);

    const [name, setName] = useState("");
    const [description, setDescription] =
        useState("");

    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    const [error, setError] = useState("");
    const [message, setMessage] = useState("");

    const loadProjects = useCallback(async () => {
        try {
            setLoading(true);
            setError("");

            const response =
                await getProjects(user.organizationId);

            setProjects(response.data || []);
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                "Failed to load projects."
            );
        } finally {
            setLoading(false);
        }
    }, [user.organizationId]);

    useEffect(() => {
        const run = async () => {
            await loadProjects();
        };

        run();
    }, [loadProjects]);

    const handleCreate = async (event) => {
        event.preventDefault();

        try {
            setSaving(true);
            setError("");
            setMessage("");

            await createProject({
                organizationId: user.organizationId,
                name,
                description,
            });

            setName("");
            setDescription("");
            setShowForm(false);
            setMessage(
                "Project created successfully."
            );

            await loadProjects();
        } catch (err) {
            setError(
                err?.response?.data?.message ||
                "Failed to create project."
            );
        } finally {
            setSaving(false);
        }
    };

    return (
        <div>
            <div className="page-header">
                <div>
                    <h2>Projects</h2>

                    <p className="page-description">
                        Manage projects in your organization.
                    </p>
                </div>

                <button
                    className="primary-button"
                    onClick={() => {
                        setShowForm(!showForm);
                        setMessage("");
                        setError("");
                    }}
                >
                    {showForm ? "Cancel" : "+ New Project"}
                </button>
            </div>

            {message && (
                <div className="success-banner">
                    {message}
                </div>
            )}

            {error && (
                <div className="error-banner">
                    {error}
                </div>
            )}

            {showForm && (
                <form
                    className="page-card form-card"
                    onSubmit={handleCreate}
                >
                    <h3>Create Project</h3>

                    <label>Project Name</label>

                    <input
                        className="form-input"
                        value={name}
                        onChange={(event) =>
                            setName(event.target.value)
                        }
                        required
                        maxLength={150}
                        placeholder="e.g. Payment Processing"
                    />

                    <label>Description</label>

                    <textarea
                        className="form-input"
                        value={description}
                        onChange={(event) =>
                            setDescription(event.target.value)
                        }
                        maxLength={500}
                        rows={4}
                        placeholder="Project description"
                    />

                    <button
                        className="primary-button"
                        disabled={saving}
                    >
                        {saving
                            ? "Creating..."
                            : "Create Project"}
                    </button>
                </form>
            )}

            <div className="page-card">
                <div className="section-title-row">
                    <h3>Your Projects</h3>

                    <span className="badge badge-blue">
            {projects.length} projects
          </span>
                </div>

                {loading ? (
                    <div className="empty">
                        Loading projects...
                    </div>
                ) : projects.length === 0 ? (
                    <div className="empty">
                        No projects found.
                    </div>
                ) : (
                    <div className="project-grid">
                        {projects.map((project) => (
                            <div
                                key={project.id}
                                className="project-card"
                            >
                                <div className="project-icon">
                                    {project.name
                                        .charAt(0)
                                        .toUpperCase()}
                                </div>

                                <div className="project-content">
                                    <h3>{project.name}</h3>

                                    <p>
                                        {project.description ||
                                            "No description provided."}
                                    </p>

                                    <span className="item-muted">
                    Project #{project.id}
                  </span>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}

export default Projects;