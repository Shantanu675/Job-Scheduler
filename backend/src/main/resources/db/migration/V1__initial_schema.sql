-- ============================================================
-- Distributed Job Scheduler
-- Initial Database Schema
-- MySQL 8.4
-- ============================================================


-- ============================================================
-- USERS
-- ============================================================

CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,

                       username VARCHAR(100) NOT NULL,
                       email VARCHAR(255) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,

                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                           ON UPDATE CURRENT_TIMESTAMP,

                       CONSTRAINT uk_users_username UNIQUE (username),
                       CONSTRAINT uk_users_email UNIQUE (email)
);


-- ============================================================
-- ORGANIZATIONS
-- ============================================================

CREATE TABLE organizations (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,

                               name VARCHAR(150) NOT NULL,

                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP,

                               CONSTRAINT uk_organizations_name UNIQUE (name)
);


-- ============================================================
-- ORGANIZATION MEMBERS
-- ============================================================

CREATE TABLE organization_members (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                      organization_id BIGINT NOT NULL,
                                      user_id BIGINT NOT NULL,

                                      role VARCHAR(30) NOT NULL DEFAULT 'MEMBER',

                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                      CONSTRAINT fk_org_members_organization
                                          FOREIGN KEY (organization_id)
                                              REFERENCES organizations(id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT fk_org_members_user
                                          FOREIGN KEY (user_id)
                                              REFERENCES users(id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT uk_org_members
                                          UNIQUE (organization_id, user_id)
);


CREATE INDEX idx_org_members_user
    ON organization_members(user_id);


-- ============================================================
-- PROJECTS
-- ============================================================

CREATE TABLE projects (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,

                          organization_id BIGINT NOT NULL,

                          name VARCHAR(150) NOT NULL,
                          description VARCHAR(500),

                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                              ON UPDATE CURRENT_TIMESTAMP,

                          CONSTRAINT fk_projects_organization
                              FOREIGN KEY (organization_id)
                                  REFERENCES organizations(id)
                                  ON DELETE CASCADE,

                          CONSTRAINT uk_projects_org_name
                              UNIQUE (organization_id, name)
);


CREATE INDEX idx_projects_organization
    ON projects(organization_id);


-- ============================================================
-- RETRY POLICIES
-- ============================================================

CREATE TABLE retry_policies (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                project_id BIGINT NOT NULL,

                                name VARCHAR(100) NOT NULL,

                                max_retries INT NOT NULL DEFAULT 3,

                                backoff_type VARCHAR(30) NOT NULL DEFAULT 'EXPONENTIAL',

                                initial_delay_ms BIGINT NOT NULL DEFAULT 1000,

                                max_delay_ms BIGINT NOT NULL DEFAULT 60000,

                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_retry_policies_project
                                    FOREIGN KEY (project_id)
                                        REFERENCES projects(id)
                                        ON DELETE CASCADE,

                                CONSTRAINT chk_retry_max
                                    CHECK (max_retries >= 0),

                                CONSTRAINT chk_retry_initial_delay
                                    CHECK (initial_delay_ms >= 0),

                                CONSTRAINT chk_retry_max_delay
                                    CHECK (max_delay_ms >= 0),

                                CONSTRAINT uk_retry_policy_project_name
                                    UNIQUE (project_id, name)
);


CREATE INDEX idx_retry_policies_project
    ON retry_policies(project_id);


-- ============================================================
-- QUEUES
-- ============================================================

CREATE TABLE queues (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,

                        project_id BIGINT NOT NULL,

                        name VARCHAR(100) NOT NULL,

                        priority INT NOT NULL DEFAULT 0,

                        max_concurrency INT NOT NULL DEFAULT 10,

                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP,

                        CONSTRAINT fk_queues_project
                            FOREIGN KEY (project_id)
                                REFERENCES projects(id)
                                ON DELETE CASCADE,

                        CONSTRAINT chk_queue_concurrency
                            CHECK (max_concurrency > 0),

                        CONSTRAINT uk_queue_project_name
                            UNIQUE (project_id, name)
);


CREATE INDEX idx_queues_project
    ON queues(project_id);


-- ============================================================
-- WORKERS
-- ============================================================

CREATE TABLE workers (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,

                         worker_id VARCHAR(100) NOT NULL,

                         hostname VARCHAR(255),

                         status VARCHAR(30) NOT NULL DEFAULT 'OFFLINE',

                         last_heartbeat_at TIMESTAMP NULL,

                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                             ON UPDATE CURRENT_TIMESTAMP,

                         CONSTRAINT uk_workers_worker_id
                             UNIQUE (worker_id)
);


CREATE INDEX idx_workers_status
    ON workers(status);


CREATE INDEX idx_workers_heartbeat
    ON workers(last_heartbeat_at);


-- ============================================================
-- WORKER HEARTBEATS
-- ============================================================

CREATE TABLE worker_heartbeats (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                   worker_id BIGINT NOT NULL,

                                   heartbeat_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   cpu_usage DECIMAL(5,2),
                                   memory_usage DECIMAL(5,2),

                                   CONSTRAINT fk_worker_heartbeats_worker
                                       FOREIGN KEY (worker_id)
                                           REFERENCES workers(id)
                                           ON DELETE CASCADE
);


CREATE INDEX idx_worker_heartbeats_worker_time
    ON worker_heartbeats(worker_id, heartbeat_at);


-- ============================================================
-- JOBS
-- ============================================================

CREATE TABLE jobs (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,

                      project_id BIGINT NOT NULL,
                      queue_id BIGINT NOT NULL,
                      retry_policy_id BIGINT NULL,
                      worker_id BIGINT NULL,

                      job_type VARCHAR(100) NOT NULL,

                      payload JSON,

                      status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

                      priority INT NOT NULL DEFAULT 0,

                      retry_count INT NOT NULL DEFAULT 0,
                      max_retries INT NOT NULL DEFAULT 3,

                      available_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                      claimed_at TIMESTAMP NULL,
                      started_at TIMESTAMP NULL,
                      completed_at TIMESTAMP NULL,

                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                          ON UPDATE CURRENT_TIMESTAMP,

                      CONSTRAINT fk_jobs_project
                          FOREIGN KEY (project_id)
                              REFERENCES projects(id)
                              ON DELETE CASCADE,

                      CONSTRAINT fk_jobs_queue
                          FOREIGN KEY (queue_id)
                              REFERENCES queues(id)
                              ON DELETE CASCADE,

                      CONSTRAINT fk_jobs_retry_policy
                          FOREIGN KEY (retry_policy_id)
                              REFERENCES retry_policies(id)
                              ON DELETE SET NULL,

                      CONSTRAINT fk_jobs_worker
                          FOREIGN KEY (worker_id)
                              REFERENCES workers(id)
                              ON DELETE SET NULL,

                      CONSTRAINT chk_jobs_retry_count
                          CHECK (retry_count >= 0),

                      CONSTRAINT chk_jobs_max_retries
                          CHECK (max_retries >= 0)
);


CREATE INDEX idx_jobs_claim
    ON jobs(queue_id, status, available_at, priority, id);

CREATE INDEX idx_jobs_project
    ON jobs(project_id);

CREATE INDEX idx_jobs_worker
    ON jobs(worker_id);

CREATE INDEX idx_jobs_status
    ON jobs(status);

CREATE INDEX idx_jobs_available
    ON jobs(available_at);
-- ============================================================
-- JOB EXECUTIONS
-- ============================================================

CREATE TABLE job_executions (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                job_id BIGINT NOT NULL,

                                worker_id BIGINT NULL,

                                attempt_number INT NOT NULL DEFAULT 1,

                                status VARCHAR(30) NOT NULL,

                                started_at TIMESTAMP NULL,

                                completed_at TIMESTAMP NULL,

                                duration_ms BIGINT NULL,

                                error_message TEXT,

                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_job_executions_job
                                    FOREIGN KEY (job_id)
                                        REFERENCES jobs(id)
                                        ON DELETE CASCADE,

                                CONSTRAINT fk_job_executions_worker
                                    FOREIGN KEY (worker_id)
                                        REFERENCES workers(id)
                                        ON DELETE SET NULL,

                                CONSTRAINT chk_execution_attempt
                                    CHECK (attempt_number > 0)
);


CREATE INDEX idx_job_executions_job
    ON job_executions(job_id);


CREATE INDEX idx_job_executions_worker
    ON job_executions(worker_id);


CREATE INDEX idx_job_executions_status
    ON job_executions(status);


-- ============================================================
-- JOB LOGS
-- ============================================================

CREATE TABLE job_logs (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,

                          job_id BIGINT NOT NULL,

                          execution_id BIGINT NULL,

                          level VARCHAR(20) NOT NULL DEFAULT 'INFO',

                          message TEXT NOT NULL,

                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_job_logs_job
                              FOREIGN KEY (job_id)
                                  REFERENCES jobs(id)
                                  ON DELETE CASCADE,

                          CONSTRAINT fk_job_logs_execution
                              FOREIGN KEY (execution_id)
                                  REFERENCES job_executions(id)
                                  ON DELETE SET NULL
);


CREATE INDEX idx_job_logs_job_time
    ON job_logs(job_id, created_at);


CREATE INDEX idx_job_logs_execution
    ON job_logs(execution_id);


-- ============================================================
-- SCHEDULED JOBS
-- ============================================================

CREATE TABLE scheduled_jobs (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                project_id BIGINT NOT NULL,
                                queue_id BIGINT NOT NULL,

                                job_type VARCHAR(100) NOT NULL,

                                payload JSON,

                                cron_expression VARCHAR(100) NOT NULL,

                                timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',

                                enabled BOOLEAN NOT NULL DEFAULT TRUE,

                                next_run_at TIMESTAMP NULL,

                                last_run_at TIMESTAMP NULL,

                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP,

                                CONSTRAINT fk_scheduled_jobs_project
                                    FOREIGN KEY (project_id)
                                        REFERENCES projects(id)
                                        ON DELETE CASCADE,

                                CONSTRAINT fk_scheduled_jobs_queue
                                    FOREIGN KEY (queue_id)
                                        REFERENCES queues(id)
                                        ON DELETE CASCADE
);


CREATE INDEX idx_scheduled_jobs_next_run
    ON scheduled_jobs(enabled, next_run_at);


CREATE INDEX idx_scheduled_jobs_project
    ON scheduled_jobs(project_id);


-- ============================================================
-- DEAD LETTER QUEUE
-- ============================================================

CREATE TABLE dead_letter_queue (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                   job_id BIGINT NOT NULL,

                                   reason VARCHAR(255),

                                   final_error TEXT,

                                   retry_count INT NOT NULL,

                                   moved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT fk_dlq_job
                                       FOREIGN KEY (job_id)
                                           REFERENCES jobs(id)
                                           ON DELETE CASCADE
);


CREATE INDEX idx_dlq_job
    ON dead_letter_queue(job_id);


CREATE INDEX idx_dlq_moved_at
    ON dead_letter_queue(moved_at);