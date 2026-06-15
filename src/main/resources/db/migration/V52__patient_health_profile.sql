-- Extend medical_records with clinical fields
ALTER TABLE medical_records ADD COLUMN IF NOT EXISTS blood_type VARCHAR(15);
ALTER TABLE medical_records ADD COLUMN IF NOT EXISTS height     DECIMAL(5,2);
ALTER TABLE medical_records ADD COLUMN IF NOT EXISTS weight     DECIMAL(5,2);

-- Convert emergency contacts from 1:1 to 1:N
ALTER TABLE emergency_contacts DROP CONSTRAINT IF EXISTS emergency_contacts_patient_profile_id_key;
ALTER TABLE emergency_contacts ADD COLUMN IF NOT EXISTS relationship VARCHAR(20);
ALTER TABLE emergency_contacts ADD COLUMN IF NOT EXISTS email       VARCHAR(255);
CREATE INDEX IF NOT EXISTS idx_emergency_contacts_patient ON emergency_contacts(patient_profile_id);

-- Vaccines
CREATE TABLE IF NOT EXISTS patient_vaccines (
    id                 VARCHAR(36)  PRIMARY KEY,
    patient_profile_id VARCHAR(36)  NOT NULL REFERENCES patient_profiles(id) ON DELETE CASCADE,
    vaccine_name       VARCHAR(255) NOT NULL,
    dose_number        VARCHAR(20),
    administered_at    DATE,
    notes              VARCHAR(500),
    created_at         TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_vaccines_patient ON patient_vaccines(patient_profile_id);

-- Documents
CREATE TABLE IF NOT EXISTS patient_documents (
    id                 VARCHAR(36)  PRIMARY KEY,
    patient_profile_id VARCHAR(36)  NOT NULL REFERENCES patient_profiles(id) ON DELETE CASCADE,
    document_type      VARCHAR(30)  NOT NULL,
    document_label     VARCHAR(100),
    file_url           VARCHAR(500) NOT NULL,
    file_name          VARCHAR(255),
    uploaded_at        TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_documents_patient ON patient_documents(patient_profile_id);
