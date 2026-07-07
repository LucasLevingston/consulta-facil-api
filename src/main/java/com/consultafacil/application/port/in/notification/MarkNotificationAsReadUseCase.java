package com.consultafacil.application.port.in.notification;

import com.consultafacil.api.dto.notification.NotificationResponseDTO;

public interface MarkNotificationAsReadUseCase {

    NotificationResponseDTO execute(String notificationId, String userId);
}
