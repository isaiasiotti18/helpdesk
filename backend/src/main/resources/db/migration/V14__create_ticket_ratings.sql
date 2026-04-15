CREATE TABLE ticket_ratings (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id   UUID         NOT NULL REFERENCES tickets(id) UNIQUE,
    user_id     UUID         NOT NULL REFERENCES users(id),
    score       INT          NOT NULL CHECK (score >= 1 AND score <= 5),
    comment     TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ticket_ratings_ticket ON ticket_ratings(ticket_id);
CREATE INDEX idx_ticket_ratings_score  ON ticket_ratings(score);
