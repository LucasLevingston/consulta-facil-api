-- V9: Notifications for clinic invites and other events

CREATE TABLE notifications (
    id VARCHAR(36) PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    target_user_id VARCHAR(36) NOT NULL,
    clinic_id VARCHAR(36),
    doctor_profile_id VARCHAR(36),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (target_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (clinic_id) REFERENCES clinics(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_profile_id) REFERENCES doctor_profiles(id) ON DELETE CASCADE
);

CREATE INDEX idx_notification_target_user ON notifications(target_user_id);
CREATE INDEX idx_notification_status ON notifications(status);
