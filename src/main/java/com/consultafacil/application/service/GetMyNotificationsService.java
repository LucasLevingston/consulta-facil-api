package com.consultafacil.application.service;

import com.consultafacil.api.dto.notification.NotificationResponseDTO;
import com.consultafacil.domain.port.out.NotificationRepositoryPort;
import com.consultafacil.application.port.in.GetMyNotificationsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetMyNotificationsService implements GetMyNotificationsUseCase {

    private final NotificationRepositoryPort notificationRepository;
    private final NotificationMapper notificationMapper;

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> execute(String userId) {
        return notificationRepository.findByTargetUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(notificationMapper::toResponseDTO)
                .toList();
    }
}
