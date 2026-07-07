package com.consultafacil.domain.repository.analytics;

import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentAnalyticsRepository extends JpaRepository<Appointment, String> {

    long countByStatus(AppointmentStatus status);

    @Query("SELECT YEAR(a.scheduledAt), MONTH(a.scheduledAt), COUNT(a) FROM Appointment a " +
           "WHERE a.scheduledAt >= :since " +
           "GROUP BY YEAR(a.scheduledAt), MONTH(a.scheduledAt) " +
           "ORDER BY YEAR(a.scheduledAt), MONTH(a.scheduledAt)")
    List<Object[]> countByMonth(@Param("since") LocalDateTime since);

    @Query("SELECT a.status, COUNT(a) FROM Appointment a GROUP BY a.status")
    List<Object[]> groupByStatus();

    @Query("SELECT a.modality, COUNT(a) FROM Appointment a GROUP BY a.modality")
    List<Object[]> groupByModality();
}
