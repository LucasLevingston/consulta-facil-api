CREATE TABLE procedure_requests (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    service_id VARCHAR(36) NOT NULL,
    patient_id VARCHAR(36) NOT NULL,
    professional_id VARCHAR(36) NOT NULL,
    appointment_id VARCHAR(36),
    notes TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_pr_service FOREIGN KEY (service_id) REFERENCES professional_services(id),
    CONSTRAINT fk_pr_patient FOREIGN KEY (patient_id) REFERENCES patient_profiles(id),
    CONSTRAINT fk_pr_professional FOREIGN KEY (professional_id) REFERENCES professional_profiles(id),
    CONSTRAINT fk_pr_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);
