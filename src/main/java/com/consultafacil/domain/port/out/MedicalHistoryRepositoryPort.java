package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.MedicalHistory;

import java.util.Optional;

public interface MedicalHistoryRepositoryPort {

    MedicalHistory save(MedicalHistory history);

    Optional<MedicalHistory> findByAppointmentId(String appointmentId);
}
