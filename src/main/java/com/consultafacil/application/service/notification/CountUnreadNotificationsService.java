package com.consultafacil.application.service.notification;

import com.consultafacil.domain.enums.NotificationStatus;
import com.consultafacil.domain.port.out.NotificationRepositoryPort;
import com.consultafacil.application.port.in.CountUnreadNotificationsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CountUnreadNotificationsService implements CountUnreadNotificationsUseCase {

    private final NotificationRepositoryPort notificationRepository;

    @Transactional(readOnly = true)
    public long execute(String userId) {
        return notificationRepository.countByTargetUserIdAndStatusIn(userId,
                List.of(NotificationStatus.PENDING));
    }
}
