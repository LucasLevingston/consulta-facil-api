package com.consultafacil.application.service;

import com.consultafacil.api.dto.notification.NotificationResponseDTO;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Notification;
import com.consultafacil.domain.enums.NotificationStatus;
import com.consultafacil.domain.port.out.NotificationRepositoryPort;
import com.consultafacil.application.port.in.MarkNotificationAsReadUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarkNotificationAsReadService implements MarkNotificationAsReadUseCase {

    private final NotificationRepositoryPort notificationRepository;
    private final NotificationMapper notificationMapper;

    @Transactional
    public NotificationResponseDTO execute(String notificationId, String userId) {
        Notification notification = getAndValidate(notificationId, userId);
        if (notification.getStatus() == NotificationStatus.PENDING) {
            notification.setStatus(NotificationStatus.READ);
            notificationRepository.save(notification);
        }
        return notificationMapper.toResponseDTO(notification);
    }

    private Notification getAndValidate(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        if (!notification.getTargetUser().getId().equals(userId)) {
            throw new BadRequestException("You do not have permission to access this notification");
        }
        return notification;
    }
}
