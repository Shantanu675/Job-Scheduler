ALTER TABLE queues
    ADD COLUMN retry_policy_id BIGINT NULL;

ALTER TABLE queues
    ADD CONSTRAINT fk_queues_retry_policy
        FOREIGN KEY (retry_policy_id)
        REFERENCES retry_policies(id)
        ON DELETE SET NULL;

CREATE INDEX idx_queues_retry_policy
    ON queues(retry_policy_id);
