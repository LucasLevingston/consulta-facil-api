CREATE TABLE patient_insurance (
    id VARCHAR(36) NOT NULL,
    patient_id VARCHAR(36) NOT NULL,
    plan_id VARCHAR(36) NOT NULL,
    member_number VARCHAR(50) NOT NULL,
    holder_name VARCHAR(100) NOT NULL,
    valid_until DATE,
    CONSTRAINT pk_patient_insurance PRIMARY KEY (id),
    CONSTRAINT fk_patient_insurance_patient FOREIGN KEY (patient_id) REFERENCES patient_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_patient_insurance_plan FOREIGN KEY (plan_id) REFERENCES health_insurance_plans(id) ON DELETE CASCADE
);
CREATE INDEX idx_patient_insurance_patient ON patient_insurance (patient_id);
