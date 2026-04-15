ALTER TABLE messages ADD COLUMN is_internal BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX idx_messages_internal ON messages(session_id, is_internal);