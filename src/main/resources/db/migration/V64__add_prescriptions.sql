CREATE TABLE prescriptions (
    id VARCHAR(36) NOT NULL,
    appointment_id VARCHAR(36) NOT NULL,
    professional_id VARCHAR(36) NOT NULL,
    patient_id VARCHAR(36) NOT NULL,
    medications TEXT NOT NULL,
    instructions TEXT,
    valid_until DATE,
    pdf_url VARCHAR(500),
    pdf_key VARCHAR(300),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT pk_prescriptions PRIMARY KEY (id),
    CONSTRAINT fk_prescription_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    CONSTRAINT fk_prescription_professional FOREIGN KEY (professional_id) REFERENCES professional_profiles(id),
    CONSTRAINT fk_prescription_patient FOREIGN KEY (patient_id) REFERENCES patient_profiles(id)
);
CREATE INDEX idx_prescription_appointment ON prescriptions (appointment_id);
CREATE INDEX idx_prescription_patient ON prescriptions (patient_id);
CREATE INDEX idx_prescription_professional ON prescriptions (professional_id);
