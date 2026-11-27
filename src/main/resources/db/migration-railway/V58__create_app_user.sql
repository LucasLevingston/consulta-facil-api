-- Creates cfapp role with DML-only permissions (no DDL).
-- Flyway runs this migration as FLYWAY_DB_USERNAME (DDL admin = Railway PGUSER).
-- App runtime connects as cfapp: DB_USERNAME=cfapp, DB_PASSWORD=<value>.
-- ${app-db-password} is a Flyway placeholder resolved from DB_PASSWORD env var.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'cfapp') THEN
        EXECUTE format('CREATE ROLE cfapp WITH LOGIN PASSWORD %L', '${app-db-password}');
    END IF;
    EXECUTE format('ALTER ROLE cfapp PASSWORD %L', '${app-db-password}');
END
$$;

DO $$
BEGIN
    EXECUTE format('GRANT CONNECT ON DATABASE %I TO cfapp', current_database());
END
$$;

GRANT USAGE ON SCHEMA public TO cfapp;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO cfapp;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO cfapp;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO cfapp;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO cfapp;
