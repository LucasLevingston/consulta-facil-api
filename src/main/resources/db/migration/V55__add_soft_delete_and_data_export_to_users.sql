-- LGPD art. 18, VI: soft-delete and data-export tracking on users table.
ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP;
ALTER TABLE users ADD COLUMN last_data_export_at TIMESTAMP;
