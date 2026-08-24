ALTER TABLE users
    ADD COLUMN organization_id BIGINT NULL;

ALTER TABLE users
    ADD CONSTRAINT fk_users_organization
    FOREIGN KEY (organization_id)
    REFERENCES organizations(id);

CREATE INDEX idx_users_organization
    ON users(organization_id);
