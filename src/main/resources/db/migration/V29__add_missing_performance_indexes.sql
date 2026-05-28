-- FK indexes on patient_profiles and professional_profiles
-- (user_id is used in findByUserId — called on every authenticated request)
CREATE INDEX IF NOT EXISTS idx_patient_profiles_user_id       ON patient_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_professional_profiles_user_id  ON professional_profiles(user_id);

-- Professional services — queried by professional + active flag on every catalog load
CREATE INDEX IF NOT EXISTS idx_professional_services_prof_id  ON professional_services(professional_id);
CREATE INDEX IF NOT EXISTS idx_professional_services_active   ON professional_services(active);

-- Procedure requests — queried by patient and by professional
CREATE INDEX IF NOT EXISTS idx_procedure_requests_patient_id      ON procedure_requests(patient_id);
CREATE INDEX IF NOT EXISTS idx_procedure_requests_professional_id ON procedure_requests(professional_id);
CREATE INDEX IF NOT EXISTS idx_procedure_requests_status          ON procedure_requests(status);

-- Composite index: professional schedule range queries (professional_id + scheduled_at + status)
CREATE INDEX IF NOT EXISTS idx_appointments_prof_date
    ON appointments(professional_id, scheduled_at);

-- Appointment status for reminder scheduler (status IN (...) AND scheduled_at BETWEEN ...)
CREATE INDEX IF NOT EXISTS idx_appointments_status_date
    ON appointments(status, scheduled_at);
