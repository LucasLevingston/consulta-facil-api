package com.example.consulta.domain.repository;

import com.example.consulta.domain.entity.MedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory, String> {
    Optional<MedicalHistory> findByAppointmentId(String appointmentId);
}
