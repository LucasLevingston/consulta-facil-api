-- Creates the application DB user (cfapp) with minimal privileges.
-- Flyway runs this as the master user (cfadmin).
-- After first deploy, change the password to match the SSM value:
--   psql -h <RDS_HOST> -U cfadmin -c "ALTER ROLE cfapp PASSWORD '<APP_DB_PASSWORD_from_SSM>';"

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'cfapp') THEN
        CREATE ROLE cfapp WITH LOGIN PASSWORD 'CHANGE_ME_after_first_deploy';
    END IF;
END
$$;

GRANT CONNECT ON DATABASE consulta_facil_db TO cfapp;
GRANT USAGE ON SCHEMA public TO cfapp;

-- DML only — no DDL (no CREATE TABLE, DROP, ALTER, TRUNCATE)
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO cfapp;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO cfapp;

-- Apply to future tables/sequences created by Flyway migrations
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO cfapp;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO cfapp;
