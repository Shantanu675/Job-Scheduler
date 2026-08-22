package com.scheduler.backend.job;

public enum JobStatus {

    PENDING,
    CLAIMED,
    RUNNING,
    SUCCESS,
    FAILED,
    RETRYING,
    CANCELLED
}