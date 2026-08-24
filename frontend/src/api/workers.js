import api from "./client";

export const getWorkers = () =>
    api.get("/workers");