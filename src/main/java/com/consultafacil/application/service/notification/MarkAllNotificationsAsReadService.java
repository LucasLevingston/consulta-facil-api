package com.consultafacil.application.service.notification;

import com.consultafacil.domain.port.out.notification.NotificationRepositoryPort;
import com.consultafacil.application.port.in.notification.MarkAllNotificationsAsReadUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarkAllNotificationsAsReadService implements MarkAllNotificationsAsReadUseCase {

    private final NotificationRepositoryPort notificationRepository;

    @Transactional
    public void execute(String userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }
}
