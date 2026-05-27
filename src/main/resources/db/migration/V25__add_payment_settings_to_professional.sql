ALTER TABLE professional_profiles
    ADD COLUMN payment_timing VARCHAR(30) NOT NULL DEFAULT 'AT_CONSULTATION';

CREATE TABLE professional_payment_methods (
    professional_id VARCHAR(36) NOT NULL,
    payment_method  VARCHAR(30) NOT NULL,
    PRIMARY KEY (professional_id, payment_method),
    CONSTRAINT fk_ppm_professional
        FOREIGN KEY (professional_id) REFERENCES professional_profiles(id) ON DELETE CASCADE
);
