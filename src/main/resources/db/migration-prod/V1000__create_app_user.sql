-- Creates the application DB user (cfapp) with minimal privileges.
-- Flyway runs this as the master user (cfadmin/FLYWAY_DB_USERNAME).
-- Password injected via 'app_db_password' Flyway placeholder (= DB_PASSWORD env var = cfapp's password from SSM).

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'cfapp') THEN
        CREATE ROLE cfapp WITH LOGIN PASSWORD '${app_db_password}';
    ELSE
        ALTER ROLE cfapp PASSWORD '${app_db_password}';
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
