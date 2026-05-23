CREATE TABLE clinic_working_hours (
    id VARCHAR(36) NOT NULL,
    clinic_id VARCHAR(36) NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    open_time TIME NOT NULL,
    close_time TIME NOT NULL,
    is_open BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_clinic_working_hours PRIMARY KEY (id),
    CONSTRAINT fk_clinic_wh_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id) ON DELETE CASCADE,
    CONSTRAINT uq_clinic_wh_day UNIQUE (clinic_id, day_of_week)
);

CREATE INDEX idx_clinic_working_hours_clinic_id ON clinic_working_hours(clinic_id);
