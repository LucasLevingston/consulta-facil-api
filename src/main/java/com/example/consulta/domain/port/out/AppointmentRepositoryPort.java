package com.example.consulta.domain.port.out;

import com.example.consulta.domain.PatientSummary;
import com.example.consulta.domain.entity.Appointment;
import com.example.consulta.domain.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// NOTE: Page/Pageable are Spring types — pragmatic compromise for pagination support.
public interface AppointmentRepositoryPort {

    Appointment save(Appointment appointment);

    Optional<Appointment> findById(String id);

    Page<Appointment> findByPatientId(String patientId, Pageable pageable);

    Page<Appointment> findByProfessionalId(String professionalId, Pageable pageable);

    List<Appointment> findByPatientIdAndStatus(String patientId, AppointmentStatus status);

    List<Appointment> findByProfessionalIdAndStatusAndScheduledAtBetween(
            String professionalId, AppointmentStatus status, LocalDateTime start, LocalDateTime end);

    boolean existsByProfessionalIdAndScheduledAt(String professionalId, LocalDateTime scheduledAt);

    List<Appointment> findByProfessionalIdAndStatusInAndScheduledAtBetweenOrderByCheckedInAt(
            String professionalId, List<AppointmentStatus> statuses, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByStatusInAndScheduledAtBetweenOrderByCheckedInAt(
            List<AppointmentStatus> statuses, LocalDateTime start, LocalDateTime end);

    List<Appointment> findClinicQueueAppointments(
            String clinicId, List<AppointmentStatus> statuses, LocalDateTime start, LocalDateTime end);

    Page<PatientSummary> findProfessionalPatientsByName(
            String professionalId, String search, Pageable pageable);

    Page<PatientSummary> findProfessionalPatientsByRecent(
            String professionalId, String search, Pageable pageable);

    void delete(Appointment appointment);
}
