-- LGPD / user preference: opt-out of optional appointment emails.
ALTER TABLE users ADD COLUMN email_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE;
