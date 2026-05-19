package com.example.consulta.domain.repository;

import com.example.consulta.api.dto.appointment.PatientSummaryDTO;
import com.example.consulta.domain.entity.Appointment;
import com.example.consulta.domain.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;



@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, String> {

    Page<Appointment> findByPatientId(String patientId, Pageable pageable);
    Page<Appointment> findByDoctorId(String doctorId, Pageable pageable);
    List<Appointment> findByPatientIdAndStatus(String patientId, AppointmentStatus status);
    List<Appointment> findByDoctorIdAndStatusAndScheduledAtBetween(String doctorId, AppointmentStatus status, LocalDateTime start, LocalDateTime end);
    boolean existsByDoctorIdAndScheduledAt(String doctorId, LocalDateTime scheduledAt);

    @Query("""
            SELECT new com.example.consulta.api.dto.appointment.PatientSummaryDTO(
                u.id, u.name, MAX(a.scheduledAt), COUNT(a.id))
            FROM Appointment a
            JOIN a.patient p
            JOIN p.user u
            WHERE a.doctor.id = :doctorId
            AND (:search = '' OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')))
            GROUP BY u.id, u.name
            ORDER BY u.name ASC
            """)
    List<PatientSummaryDTO> findDoctorPatientsByName(
            @Param("doctorId") String doctorId,
            @Param("search") String search);

    @Query("""
            SELECT new com.example.consulta.api.dto.appointment.PatientSummaryDTO(
                u.id, u.name, MAX(a.scheduledAt), COUNT(a.id))
            FROM Appointment a
            JOIN a.patient p
            JOIN p.user u
            WHERE a.doctor.id = :doctorId
            AND (:search = '' OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')))
            GROUP BY u.id, u.name
            ORDER BY MAX(a.scheduledAt) DESC
            """)
    List<PatientSummaryDTO> findDoctorPatientsByRecent(
            @Param("doctorId") String doctorId,
            @Param("search") String search);
}
