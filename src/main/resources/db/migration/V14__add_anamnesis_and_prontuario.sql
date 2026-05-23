CREATE TABLE anamneses (
    id VARCHAR(36) NOT NULL,
    appointment_id VARCHAR(36) NOT NULL,
    chief_complaint TEXT,
    current_medications TEXT,
    allergies TEXT,
    medical_history TEXT,
    family_history TEXT,
    observations TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_anamneses PRIMARY KEY (id),
    CONSTRAINT fk_anamnese_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    CONSTRAINT uq_anamnese_appointment UNIQUE (appointment_id)
);

CREATE TABLE prontuarios (
    id VARCHAR(36) NOT NULL,
    appointment_id VARCHAR(36) NOT NULL,
    clinical_notes TEXT,
    diagnosis TEXT,
    diagnosis_cid VARCHAR(20),
    prescription TEXT,
    exam_requests TEXT,
    treatment_plan TEXT,
    follow_up_instructions TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_prontuarios PRIMARY KEY (id),
    CONSTRAINT fk_prontuario_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    CONSTRAINT uq_prontuario_appointment UNIQUE (appointment_id)
);
