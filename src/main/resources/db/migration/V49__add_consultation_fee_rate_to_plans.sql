ALTER TABLE plans
    ADD COLUMN IF NOT EXISTS consultation_fee_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0200;

UPDATE plans SET consultation_fee_rate = 0.0500 WHERE slug LIKE '%free%';
UPDATE plans SET consultation_fee_rate = 0.0300 WHERE slug LIKE '%starter%';
UPDATE plans SET consultation_fee_rate = 0.0200 WHERE slug LIKE '%pro%';
UPDATE plans SET consultation_fee_rate = 0.0100 WHERE slug LIKE '%clinic%';
