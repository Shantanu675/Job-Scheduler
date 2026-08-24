import api from "./client";

export const getRetryPolicies = (projectId) =>
    api.get(`/retry-policies?projectId=${projectId}`);

export const getRetryPolicy = (id) =>
    api.get(`/retry-policies/${id}`);

export const createRetryPolicy = (data) =>
    api.post("/retry-policies", data);