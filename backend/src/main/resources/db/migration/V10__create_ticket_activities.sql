CREATE TABLE ticket_activities (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id   UUID         NOT NULL REFERENCES tickets(id),
    user_id     UUID         REFERENCES users(id),
    action      VARCHAR(50)  NOT NULL,
    detail      JSONB,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ticket_activities_ticket ON ticket_activities(ticket_id);
CREATE INDEX idx_ticket_activities_created ON ticket_activities(created_at DESC);
