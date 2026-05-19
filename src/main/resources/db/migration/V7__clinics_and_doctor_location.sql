-- V7: Add location to doctor_profiles, create clinics and clinic_members

ALTER TABLE doctor_profiles ADD COLUMN IF NOT EXISTS city VARCHAR(100);
ALTER TABLE doctor_profiles ADD COLUMN IF NOT EXISTS state VARCHAR(50);
ALTER TABLE doctor_profiles ADD COLUMN IF NOT EXISTS address VARCHAR(255);
ALTER TABLE doctor_profiles ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION;
ALTER TABLE doctor_profiles ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;

CREATE INDEX IF NOT EXISTS idx_doctor_city ON doctor_profiles(city);

CREATE TABLE clinics (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    phone VARCHAR(20),
    address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(50),
    zip_code VARCHAR(20),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    image_url TEXT,
    owner_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_clinic_city ON clinics(city);
CREATE INDEX idx_clinic_owner ON clinics(owner_id);

CREATE TABLE clinic_members (
    clinic_id VARCHAR(36) NOT NULL,
    doctor_profile_id VARCHAR(36) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (clinic_id, doctor_profile_id),
    FOREIGN KEY (clinic_id) REFERENCES clinics(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_profile_id) REFERENCES doctor_profiles(id) ON DELETE CASCADE
);
