CREATE TABLE sla_policies (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                     VARCHAR(100) NOT NULL UNIQUE,
    priority                 VARCHAR(10)  NOT NULL CHECK (priority IN ('LOW','MEDIUM','HIGH','URGENT')),
    first_response_minutes   INT          NOT NULL,
    resolution_minutes       INT          NOT NULL,
    is_active                BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at               TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sla_policies_priority ON sla_policies(priority);

ALTER TABLE tickets ADD COLUMN sla_policy_id             UUID REFERENCES sla_policies(id);
ALTER TABLE tickets ADD COLUMN first_response_at         TIMESTAMP;
ALTER TABLE tickets ADD COLUMN sla_first_response_deadline TIMESTAMP;
ALTER TABLE tickets ADD COLUMN sla_resolution_deadline    TIMESTAMP;
ALTER TABLE tickets ADD COLUMN sla_breached              BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_tickets_sla_breach    ON tickets(sla_breached);
CREATE INDEX idx_tickets_sla_deadline  ON tickets(sla_resolution_deadline);

-- Default policies
INSERT INTO sla_policies (name, priority, first_response_minutes, resolution_minutes) VALUES
    ('Low Priority SLA',    'LOW',    480, 2880),
    ('Medium Priority SLA', 'MEDIUM', 240, 1440),
    ('High Priority SLA',   'HIGH',   60,  480),
    ('Urgent Priority SLA', 'URGENT', 15,  120);
