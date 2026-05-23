CREATE TABLE professional_schedules (
    id VARCHAR(36) NOT NULL,
    professional_profile_id VARCHAR(36) NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    consultation_duration_minutes INT NOT NULL DEFAULT 30,
    break_between_consultations_minutes INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_professional_schedules PRIMARY KEY (id),
    CONSTRAINT fk_prof_schedule_profile FOREIGN KEY (professional_profile_id) REFERENCES professional_profiles(id) ON DELETE CASCADE,
    CONSTRAINT uq_prof_schedule_day UNIQUE (professional_profile_id, day_of_week)
);

CREATE INDEX idx_prof_schedules_profile_id ON professional_schedules(professional_profile_id);
