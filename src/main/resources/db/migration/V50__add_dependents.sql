CREATE TABLE dependents (
    id               VARCHAR(36)  PRIMARY KEY,
    guardian_user_id VARCHAR(36)  NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name             VARCHAR(255) NOT NULL,
    cpf              VARCHAR(14),
    birth_date       DATE,
    gender           VARCHAR(10),
    relationship     VARCHAR(20)  NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_dependents_guardian ON dependents(guardian_user_id);
