CREATE TABLE tickets (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    status          VARCHAR(20)  NOT NULL DEFAULT 'OPEN'
                    CHECK (status IN ('OPEN','IN_QUEUE','IN_PROGRESS','TRANSFERRED','RESOLVED','CLOSED')),
    priority        VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM'
                    CHECK (priority IN ('LOW','MEDIUM','HIGH','URGENT')),
    created_by      UUID         NOT NULL REFERENCES users(id),
    assigned_agent  UUID         REFERENCES users(id),
    version         INT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    closed_at       TIMESTAMP
);

CREATE INDEX idx_tickets_status          ON tickets(status);
CREATE INDEX idx_tickets_assigned_agent  ON tickets(assigned_agent);
CREATE INDEX idx_tickets_created_by      ON tickets(created_by);
CREATE INDEX idx_tickets_created_at      ON tickets(created_at DESC);
CREATE INDEX idx_tickets_priority        ON tickets(priority);
