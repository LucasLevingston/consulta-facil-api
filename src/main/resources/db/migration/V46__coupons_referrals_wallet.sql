-- coupon_usages: tracks coupon usage on billing payments
CREATE TABLE IF NOT EXISTS coupon_usages (
    id VARCHAR(36) PRIMARY KEY,
    coupon_id VARCHAR(36) NOT NULL REFERENCES coupons(id),
    user_id VARCHAR(36) NOT NULL,
    payment_id VARCHAR(36) REFERENCES billing_payments(id),
    discount_amount DECIMAL(12,2) NOT NULL,
    used_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- referral_codes: one per user
CREATE TABLE IF NOT EXISTS referral_codes (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    code VARCHAR(30) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- referrals: tracks who referred whom
CREATE TABLE IF NOT EXISTS referrals (
    id VARCHAR(36) PRIMARY KEY,
    referrer_id VARCHAR(36) NOT NULL,
    referred_id VARCHAR(36) NOT NULL UNIQUE,
    referral_code_id VARCHAR(36) NOT NULL REFERENCES referral_codes(id),
    first_payment_id VARCHAR(36) REFERENCES billing_payments(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_no_self_referral CHECK (referrer_id != referred_id)
);

-- referral_commissions: commission for each referral's first payment
CREATE TABLE IF NOT EXISTS referral_commissions (
    id VARCHAR(36) PRIMARY KEY,
    referral_id VARCHAR(36) NOT NULL REFERENCES referrals(id),
    payment_id VARCHAR(36) NOT NULL UNIQUE REFERENCES billing_payments(id),
    amount DECIMAL(12,2) NOT NULL,
    percentage DECIMAL(5,2) NOT NULL DEFAULT 10.00,
    available_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- wallets: one per user
CREATE TABLE IF NOT EXISTS wallets (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    balance DECIMAL(12,2) NOT NULL DEFAULT 0,
    pending_balance DECIMAL(12,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- wallet_transactions: history of wallet movements
CREATE TABLE IF NOT EXISTS wallet_transactions (
    id VARCHAR(36) PRIMARY KEY,
    wallet_id VARCHAR(36) NOT NULL REFERENCES wallets(id),
    type VARCHAR(30) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    description VARCHAR(300),
    reference_id VARCHAR(36),
    reference_type VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
