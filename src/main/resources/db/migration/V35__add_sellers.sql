CREATE TABLE sellers (
    id          VARCHAR(36)    PRIMARY KEY,
    user_id     VARCHAR(36)    NOT NULL REFERENCES users(id),
    slug        VARCHAR(20)    UNIQUE NOT NULL,
    commission_rate DECIMAL(5,2) NOT NULL,
    status      VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    pix_key     VARCHAR(255),
    notes       TEXT,
    created_at  TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_sellers_user_id ON sellers(user_id);
CREATE INDEX idx_sellers_slug ON sellers(slug);

CREATE TABLE seller_sales (
    id              VARCHAR(36)   PRIMARY KEY,
    seller_id       VARCHAR(36)   NOT NULL REFERENCES sellers(id),
    subscription_id VARCHAR(36)   NOT NULL REFERENCES subscriptions(id),
    gross_amount    DECIMAL(10,2) NOT NULL,
    commission_amount DECIMAL(10,2) NOT NULL,
    month_reference DATE          NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    paid_at         TIMESTAMP,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_seller_sales_seller_id ON seller_sales(seller_id);
CREATE INDEX idx_seller_sales_month ON seller_sales(month_reference);
CREATE INDEX idx_seller_sales_status ON seller_sales(status);

ALTER TABLE subscriptions ADD COLUMN seller_id VARCHAR(36);
ALTER TABLE subscriptions ADD COLUMN referral_slug VARCHAR(20);
