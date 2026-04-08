CREATE TABLE chat_sessions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id   UUID      NOT NULL REFERENCES tickets(id),
    started_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    ended_at    TIMESTAMP,
    CONSTRAINT uk_chat_sessions_ticket UNIQUE (ticket_id)
);

CREATE TABLE messages (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id    UUID         NOT NULL REFERENCES chat_sessions(id),
    sender_id     UUID         NOT NULL REFERENCES users(id),
    content       TEXT         NOT NULL,
    message_type  VARCHAR(20)  NOT NULL DEFAULT 'TEXT'
                  CHECK (message_type IN ('TEXT','SYSTEM','FILE')),
    sent_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_messages_session_id  ON messages(session_id);
CREATE INDEX idx_messages_sent_at     ON messages(session_id, sent_at);
CREATE INDEX idx_chat_sessions_ticket ON chat_sessions(ticket_id);
