package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.Notification;
import com.consultafacil.domain.enums.NotificationStatus;

import java.util.List;
import java.util.Optional;

public interface NotificationRepositoryPort {

    Notification save(Notification notification);

    Optional<Notification> findById(String id);

    List<Notification> findByTargetUserIdOrderByCreatedAtDesc(String userId);

    long countByTargetUserIdAndStatusIn(String userId, List<NotificationStatus> statuses);

    void markAllAsReadByUserId(String userId);

    boolean existsByClinicIdAndProfessionalProfileIdAndStatus(
            String clinicId, String professionalProfileId, NotificationStatus status);
}
