import api from "./client";

export const getJobs = (queueId) =>
    api.get(`/jobs?queueId=${queueId}`);

export const getJob = (id) =>
    api.get(`/jobs/${id}`);

export const createJob = (data) =>
    api.post("/jobs", data);

export const getJobExecutions = (jobId) =>
    api.get(`/jobs/${jobId}/executions`);