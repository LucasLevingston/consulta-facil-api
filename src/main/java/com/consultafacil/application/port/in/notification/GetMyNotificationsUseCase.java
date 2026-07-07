package com.consultafacil.application.port.in.notification;

import com.consultafacil.api.dto.notification.NotificationResponseDTO;

import java.util.List;

public interface GetMyNotificationsUseCase {

    List<NotificationResponseDTO> execute(String userId);
}
