CREATE TABLE clinic_receptionists (
    id VARCHAR(36) PRIMARY KEY,
    clinic_id VARCHAR(36) NOT NULL REFERENCES clinics(id),
    user_id VARCHAR(36) NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (clinic_id, user_id)
);

CREATE INDEX idx_clinic_receptionists_clinic_id ON clinic_receptionists(clinic_id);
CREATE INDEX idx_clinic_receptionists_user_id ON clinic_receptionists(user_id);
