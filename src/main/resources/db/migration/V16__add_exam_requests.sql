CREATE TABLE exam_requests (
    id VARCHAR(36) PRIMARY KEY,
    appointment_id VARCHAR(36) NOT NULL REFERENCES appointments(id),
    professional_id VARCHAR(36) NOT NULL REFERENCES professional_profiles(id),
    patient_id VARCHAR(36) NOT NULL REFERENCES patient_profiles(id),
    exam_name VARCHAR(255) NOT NULL,
    instructions TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    file_url TEXT,
    file_name VARCHAR(255),
    professional_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_exam_requests_appointment_id ON exam_requests(appointment_id);
CREATE INDEX idx_exam_requests_patient_id ON exam_requests(patient_id);
CREATE INDEX idx_exam_requests_professional_id ON exam_requests(professional_id);
