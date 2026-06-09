CREATE TABLE coupons (
    id           VARCHAR(36)    PRIMARY KEY,
    code         VARCHAR(50)    NOT NULL UNIQUE,
    description  VARCHAR(255),
    type         VARCHAR(20)    NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    max_uses     INTEGER,
    current_uses INTEGER        NOT NULL DEFAULT 0,
    max_uses_per_user INTEGER   DEFAULT 1,
    starts_at    TIMESTAMP,
    expires_at   TIMESTAMP,
    applicable_plan_ids TEXT,
    seller_id    VARCHAR(36)    REFERENCES sellers(id),
    status       VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    created_by   VARCHAR(36)    REFERENCES users(id),
    created_at   TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE coupon_uses (
    id               VARCHAR(36)   PRIMARY KEY,
    coupon_id        VARCHAR(36)   NOT NULL REFERENCES coupons(id),
    user_id          VARCHAR(36)   NOT NULL REFERENCES users(id),
    subscription_id  VARCHAR(36)   REFERENCES subscriptions(id),
    discount_applied DECIMAL(10,2) NOT NULL,
    used_at          TIMESTAMP     NOT NULL DEFAULT NOW()
);

ALTER TABLE subscriptions ADD COLUMN coupon_id VARCHAR(36);
ALTER TABLE subscriptions ADD COLUMN discount_applied DECIMAL(10,2);
ALTER TABLE subscriptions ADD CONSTRAINT fk_subscriptions_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(id);
