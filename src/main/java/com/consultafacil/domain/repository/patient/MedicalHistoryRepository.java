package com.consultafacil.domain.repository.patient;

import com.consultafacil.domain.entity.MedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory, String> {
    Optional<MedicalHistory> findByAppointmentId(String appointmentId);
}
