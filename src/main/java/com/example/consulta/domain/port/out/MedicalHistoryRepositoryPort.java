package com.example.consulta.domain.port.out;

import com.example.consulta.domain.entity.MedicalHistory;

import java.util.Optional;

public interface MedicalHistoryRepositoryPort {

    MedicalHistory save(MedicalHistory history);

    Optional<MedicalHistory> findByAppointmentId(String appointmentId);
}
