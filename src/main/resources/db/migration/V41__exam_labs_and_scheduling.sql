CREATE TABLE exam_labs (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    phone VARCHAR(50),
    address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(10),
    zip_code VARCHAR(20),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    image_url TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE exam_lab_accepted_exams (
    exam_lab_id VARCHAR(36) NOT NULL REFERENCES exam_labs(id) ON DELETE CASCADE,
    exam_name VARCHAR(255) NOT NULL
);

CREATE TABLE exam_lab_hours (
    id VARCHAR(36) PRIMARY KEY,
    exam_lab_id VARCHAR(36) NOT NULL REFERENCES exam_labs(id) ON DELETE CASCADE,
    day_of_week VARCHAR(20) NOT NULL,
    open_time TIME NOT NULL,
    close_time TIME NOT NULL,
    slot_duration_minutes INT NOT NULL DEFAULT 30,
    is_open BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (exam_lab_id, day_of_week)
);

CREATE TABLE exam_schedulings (
    id VARCHAR(36) PRIMARY KEY,
    exam_request_id VARCHAR(36) NOT NULL REFERENCES exam_requests(id),
    exam_lab_id VARCHAR(36) NOT NULL REFERENCES exam_labs(id),
    scheduled_date DATE NOT NULL,
    scheduled_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_exam_labs_city ON exam_labs(city);
CREATE INDEX idx_exam_labs_status ON exam_labs(status);
CREATE INDEX idx_exam_lab_hours_lab ON exam_lab_hours(exam_lab_id);
CREATE INDEX idx_exam_schedulings_request ON exam_schedulings(exam_request_id);
CREATE INDEX idx_exam_schedulings_lab_date ON exam_schedulings(exam_lab_id, scheduled_date);
