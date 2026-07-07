CREATE TABLE IF NOT EXISTS conversations (
    id              VARCHAR(36)  PRIMARY KEY,
    patient_id      VARCHAR(36)  NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    professional_id VARCHAR(36)  NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE (patient_id, professional_id)
);

CREATE INDEX IF NOT EXISTS idx_conversation_patient      ON conversations(patient_id);
CREATE INDEX IF NOT EXISTS idx_conversation_professional ON conversations(professional_id);

CREATE TABLE IF NOT EXISTS messages (
    id              VARCHAR(36)  PRIMARY KEY,
    conversation_id VARCHAR(36)  NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id       VARCHAR(36)  NOT NULL REFERENCES users(id),
    content         TEXT         NOT NULL,
    sent_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    read_at         TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_message_conversation ON messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_message_sender       ON messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_message_sent_at      ON messages(conversation_id, sent_at DESC);
