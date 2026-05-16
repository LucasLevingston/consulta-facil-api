package com.example.consulta.domain.repository;

import com.example.consulta.domain.entity.Appointment;
import com.example.consulta.domain.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
