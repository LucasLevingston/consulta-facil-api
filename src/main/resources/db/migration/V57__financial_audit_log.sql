CREATE TABLE financial_audit_logs (
    id              VARCHAR(36)     NOT NULL,
    entity_type     VARCHAR(50)     NOT NULL,
    entity_id       VARCHAR(36)     NOT NULL,
    action          VARCHAR(20)     NOT NULL,
    actor_id        VARCHAR(36),
    amount          DECIMAL(12, 2),
    occurred_at     TIMESTAMP       NOT NULL,
    CONSTRAINT pk_financial_audit_logs PRIMARY KEY (id)
);

CREATE INDEX idx_financial_audit_entity ON financial_audit_logs (entity_type, entity_id);
CREATE INDEX idx_financial_audit_actor  ON financial_audit_logs (actor_id);
CREATE INDEX idx_financial_audit_time   ON financial_audit_logs (occurred_at);
