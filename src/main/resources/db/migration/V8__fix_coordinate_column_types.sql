-- V8: Fix latitude/longitude column types from DECIMAL to DOUBLE PRECISION
-- Required on databases where V7 ran with DECIMAL(10,8) columns.
-- On fresh installs (V7 already creates DOUBLE PRECISION) this is a no-op.

ALTER TABLE doctor_profiles ALTER COLUMN latitude TYPE DOUBLE PRECISION;
ALTER TABLE doctor_profiles ALTER COLUMN longitude TYPE DOUBLE PRECISION;
ALTER TABLE clinics ALTER COLUMN latitude TYPE DOUBLE PRECISION;
ALTER TABLE clinics ALTER COLUMN longitude TYPE DOUBLE PRECISION;
