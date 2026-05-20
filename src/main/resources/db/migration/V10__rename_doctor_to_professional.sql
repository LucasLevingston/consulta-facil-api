-- Rename doctor_profiles table to professional_profiles
ALTER TABLE doctor_profiles RENAME TO professional_profiles;

-- Rename doctor_id column in appointments to professional_id
ALTER TABLE appointments RENAME COLUMN doctor_id TO professional_id;

-- Rename doctor_profile_id column in clinic_members to professional_profile_id
ALTER TABLE clinic_members RENAME COLUMN doctor_profile_id TO professional_profile_id;

-- Rename doctor_profile_id column in notifications to professional_profile_id
ALTER TABLE notifications RENAME COLUMN doctor_profile_id TO professional_profile_id;

-- Update UserRole enum values: DOCTOR -> PROFESSIONAL
UPDATE users SET role = 'PROFESSIONAL' WHERE role = 'DOCTOR';

-- Rename indexes
ALTER INDEX IF EXISTS idx_specialty RENAME TO idx_professional_specialty;
ALTER INDEX IF EXISTS idx_doctor_city RENAME TO idx_professional_city;
