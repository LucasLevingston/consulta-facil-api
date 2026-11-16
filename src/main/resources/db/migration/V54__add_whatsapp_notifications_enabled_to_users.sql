-- Opt-out for WhatsApp appointment notifications (issue #5). Existing users keep receiving messages.
ALTER TABLE users ADD COLUMN whatsapp_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE;
