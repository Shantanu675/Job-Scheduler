import api from "./client";

export const getProjects = (organizationId) =>
    api.get(`/projects?organizationId=${organizationId}`);

export const getProject = (id) =>
    api.get(`/projects/${id}`);

export const createProject = (data) =>
    api.post("/projects", data);