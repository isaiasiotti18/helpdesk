CREATE TABLE queues (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    max_agents  INT          NOT NULL DEFAULT 10,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE queue_agents (
    queue_id    UUID NOT NULL REFERENCES queues(id),
    agent_id    UUID NOT NULL REFERENCES users(id),
    joined_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (queue_id, agent_id)
);

CREATE TABLE agent_status (
    agent_id       UUID PRIMARY KEY REFERENCES users(id),
    status         VARCHAR(20) NOT NULL DEFAULT 'OFFLINE'
                   CHECK (status IN ('ONLINE','BUSY','AWAY','OFFLINE')),
    active_tickets INT         NOT NULL DEFAULT 0,
    max_tickets    INT         NOT NULL DEFAULT 5,
    last_seen      TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_agent_status_status ON agent_status(status);

-- Adiciona queue_id na tabela tickets (já existente)
ALTER TABLE tickets ADD COLUMN queue_id UUID REFERENCES queues(id);
CREATE INDEX idx_tickets_queue_id     ON tickets(queue_id);
CREATE INDEX idx_tickets_queue_status ON tickets(queue_id, status);
