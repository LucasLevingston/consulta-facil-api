package com.consultafacil.domain.repository.notification;

import com.consultafacil.domain.entity.Notification;
import com.consultafacil.domain.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {

    @Query("SELECT n FROM Notification n JOIN FETCH n.clinic WHERE n.targetUser.id = :userId ORDER BY n.createdAt DESC")
    List<Notification> findByTargetUserIdOrderByCreatedAtDesc(@Param("userId") String userId);

    long countByTargetUserIdAndStatusIn(String userId, List<NotificationStatus> statuses);

    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ' WHERE n.targetUser.id = :userId AND n.status = 'PENDING'")
    void markAllAsReadByUserId(@Param("userId") String userId);

    boolean existsByClinicIdAndProfessionalProfileIdAndStatus(String clinicId, String professionalProfileId, NotificationStatus status);
}
