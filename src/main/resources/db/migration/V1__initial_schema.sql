-- V1__initial_schema.sql
-- Initial database schema for Consulta Fácil API

CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    phone VARCHAR(20),
    cpf VARCHAR(11) UNIQUE,
    birth_date DATE,
    gender VARCHAR(50),
    image_url TEXT,
    image_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email ON users(email);
CREATE INDEX idx_cpf ON users(cpf);

-- Addresses table
CREATE TABLE addresses (
    id VARCHAR(36) PRIMARY KEY,
    zip_code VARCHAR(8),
    street VARCHAR(255),
    number VARCHAR(20),
    district VARCHAR(100),
    city VARCHAR(100),
    state VARCHAR(2),
    country VARCHAR(100) DEFAULT 'Brazil',
    user_id VARCHAR(36) NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Patient Profiles table
CREATE TABLE patient_profiles (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    occupation VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Doctor Profiles table
CREATE TABLE doctor_profiles (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    specialty VARCHAR(100) NOT NULL,
    license_number VARCHAR(50) NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_specialty ON doctor_profiles(specialty);

-- Emergency Contacts table
CREATE TABLE emergency_contacts (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    patient_profile_id VARCHAR(36) NOT NULL UNIQUE,
    FOREIGN KEY (patient_profile_id) REFERENCES patient_profiles(id) ON DELETE CASCADE
);

-- Medical Records table
CREATE TABLE medical_records (
    id VARCHAR(36) PRIMARY KEY,
    allergies TEXT,
    current_medication TEXT,
    family_medical_history TEXT,
    past_medical_history TEXT,
    privacy_consent BOOLEAN NOT NULL DEFAULT false,
    treatment_consent BOOLEAN NOT NULL DEFAULT false,
    disclosure_consent BOOLEAN NOT NULL DEFAULT false,
    patient_profile_id VARCHAR(36) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_profile_id) REFERENCES patient_profiles(id) ON DELETE CASCADE
);

-- Appointments table
CREATE TABLE appointments (
    id VARCHAR(36) PRIMARY KEY,
    patient_id VARCHAR(36) NOT NULL,
    doctor_id VARCHAR(36) NOT NULL,
    scheduled_at TIMESTAMP NOT NULL,
    reason TEXT,
    notes TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    cancellation_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patient_profiles(id),
    FOREIGN KEY (doctor_id) REFERENCES doctor_profiles(id)
);

CREATE INDEX idx_patient_id ON appointments(patient_id);
CREATE INDEX idx_doctor_id ON appointments(doctor_id);
CREATE INDEX idx_scheduled_at ON appointments(scheduled_at);
CREATE INDEX idx_status ON appointments(status);
