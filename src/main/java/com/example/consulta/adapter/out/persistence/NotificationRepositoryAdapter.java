package com.example.consulta.adapter.out.persistence;

import com.example.consulta.domain.entity.Notification;
import com.example.consulta.domain.enums.NotificationStatus;
import com.example.consulta.domain.port.out.NotificationRepositoryPort;
import com.example.consulta.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final NotificationRepository notificationRepository;

    @Override
    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    public Optional<Notification> findById(String id) {
        return notificationRepository.findById(id);
    }

    @Override
    public List<Notification> findByTargetUserIdOrderByCreatedAtDesc(String userId) {
        return notificationRepository.findByTargetUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public long countByTargetUserIdAndStatusIn(String userId, List<NotificationStatus> statuses) {
        return notificationRepository.countByTargetUserIdAndStatusIn(userId, statuses);
    }

    @Override
    public void markAllAsReadByUserId(String userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    public boolean existsByClinicIdAndProfessionalProfileIdAndStatus(
            String clinicId, String professionalProfileId, NotificationStatus status) {
        return notificationRepository.existsByClinicIdAndProfessionalProfileIdAndStatus(
                clinicId, professionalProfileId, status);
    }
}
