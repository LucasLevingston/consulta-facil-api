CREATE TABLE whatsapp_conversations (
    id VARCHAR(255) PRIMARY KEY,
    phone_number VARCHAR(50) NOT NULL UNIQUE,
    history_json TEXT NOT NULL DEFAULT '[]',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_whatsapp_phone ON whatsapp_conversations(phone_number);
