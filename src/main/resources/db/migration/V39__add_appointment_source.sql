ALTER TABLE appointments ADD COLUMN source VARCHAR(20) NOT NULL DEFAULT 'ONLINE';
ALTER TABLE appointments ADD COLUMN walk_in_payment_method VARCHAR(30);
ALTER TABLE appointments ADD COLUMN duration_minutes INTEGER;
