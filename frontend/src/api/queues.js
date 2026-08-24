import api from "./client";

export const getQueues = (projectId) =>
    api.get(`/queues?projectId=${projectId}`);

export const getQueue = (id) =>
    api.get(`/queues/${id}`);

export const createQueue = (data) =>
    api.post("/queues", data);

export const updateQueue = (id, data) =>
    api.put(`/queues/${id}`, data);

export const getQueueStats = (id) =>
    api.get(`/queues/${id}/stats`);

export const pauseQueue = (id) =>
    api.post(`/queues/${id}/pause`);

export const resumeQueue = (id) =>
    api.post(`/queues/${id}/resume`);