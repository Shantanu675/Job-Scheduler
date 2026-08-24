import api from "./client";

export const getDlq = () =>
    api.get("/dlq");

export const getDlqEntry = (id) =>
    api.get(`/dlq/${id}`);

export const requeueDlq = (id) =>
    api.post(`/dlq/${id}/requeue`);