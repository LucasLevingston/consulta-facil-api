ALTER TABLE subscriptions ADD COLUMN mp_preapproval_id VARCHAR(128) UNIQUE;

CREATE INDEX idx_subscriptions_preapproval ON subscriptions(mp_preapproval_id);
