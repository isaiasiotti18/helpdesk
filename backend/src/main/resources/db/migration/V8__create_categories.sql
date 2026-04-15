CREATE TABLE categories (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    queue_id    UUID         REFERENCES queues(id),
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

ALTER TABLE tickets ADD COLUMN category_id UUID REFERENCES categories(id);

CREATE INDEX idx_categories_queue_id    ON categories(queue_id);
CREATE INDEX idx_tickets_category_id    ON tickets(category_id);
CREATE INDEX idx_categories_is_active   ON categories(is_active);
