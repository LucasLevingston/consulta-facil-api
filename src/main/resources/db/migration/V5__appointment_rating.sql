ALTER TABLE appointments ADD COLUMN rating INTEGER CHECK (rating BETWEEN 1 AND 5);
ALTER TABLE appointments ADD COLUMN rating_comment TEXT;
ALTER TABLE doctor_profiles DROP COLUMN rating;
