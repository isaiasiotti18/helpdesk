ALTER TABLE tickets ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP;

CREATE INDEX idx_tickets_deleted_at ON tickets(deleted_at);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);