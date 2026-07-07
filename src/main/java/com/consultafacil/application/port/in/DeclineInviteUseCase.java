package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.notification.NotificationResponseDTO;

public interface DeclineInviteUseCase {

    NotificationResponseDTO execute(String notificationId, String userId);
}
