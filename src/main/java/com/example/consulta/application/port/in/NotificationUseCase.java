package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.notification.NotificationResponseDTO;

import java.util.List;

public interface NotificationUseCase {

    void sendClinicInvite(String clinicId, String professionalProfileId, String requesterId);

    List<NotificationResponseDTO> getMyNotifications(String userId);

    long countUnread(String userId);

    NotificationResponseDTO markAsRead(String notificationId, String userId);

    void markAllAsRead(String userId);

    NotificationResponseDTO acceptInvite(String notificationId, String userId);

    NotificationResponseDTO declineInvite(String notificationId, String userId);
}
