CREATE TABLE magic_link_tokens (
    id           VARCHAR(36)  NOT NULL PRIMARY KEY,
    user_id      VARCHAR(36)  NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token        VARCHAR(64)  NOT NULL UNIQUE,
    expires_at   TIMESTAMP    NOT NULL,
    used         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_magic_link_token ON magic_link_tokens(token);
CREATE INDEX idx_magic_link_user  ON magic_link_tokens(user_id);
