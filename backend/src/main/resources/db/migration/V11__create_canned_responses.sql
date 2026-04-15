CREATE TABLE canned_responses (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title       VARCHAR(100) NOT NULL,
    content     TEXT         NOT NULL,
    shortcut    VARCHAR(50),
    category    VARCHAR(50),
    created_by  UUID         NOT NULL REFERENCES users(id),
    is_shared   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_canned_responses_created_by ON canned_responses(created_by);
CREATE INDEX idx_canned_responses_shared     ON canned_responses(is_shared);
CREATE INDEX idx_canned_responses_shortcut   ON canned_responses(shortcut);
