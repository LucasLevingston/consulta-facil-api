-- Add profession column to professional_profiles
ALTER TABLE professional_profiles ADD COLUMN profession VARCHAR(100);

CREATE INDEX idx_profession ON professional_profiles(profession);
