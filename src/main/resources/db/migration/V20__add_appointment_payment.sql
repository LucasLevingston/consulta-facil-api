ALTER TABLE appointments ADD COLUMN payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID';
ALTER TABLE appointments ADD COLUMN payment_amount NUMERIC(10,2);
ALTER TABLE appointments ADD COLUMN payment_preference_id VARCHAR(255);
ALTER TABLE appointments ADD COLUMN payment_id VARCHAR(255);
