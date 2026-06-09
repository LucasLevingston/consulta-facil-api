CREATE TABLE subscription_payments (
    id              VARCHAR(36)   PRIMARY KEY,
    subscription_id VARCHAR(36)   NOT NULL,
    mp_payment_id   VARCHAR(255),
    gross_amount    DECIMAL(10,2) NOT NULL,
    processing_fee  DECIMAL(10,2),
    tax_amount      DECIMAL(10,2),
    iss_amount      DECIMAL(10,2),
    net_amount      DECIMAL(10,2),
    tax_rate_applied DECIMAL(5,2),
    tax_regime      VARCHAR(30),
    payment_method  VARCHAR(30),
    fiscal_document_id VARCHAR(255),
    tax_snapshot    TEXT,
    paid_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id)
);

CREATE INDEX idx_sub_payments_subscription_id ON subscription_payments(subscription_id);
CREATE INDEX idx_sub_payments_paid_at ON subscription_payments(paid_at);
