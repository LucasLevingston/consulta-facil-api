package com.consultafacil.adapter.out.persistence.patient;

import com.consultafacil.domain.entity.MedicalHistory;
import com.consultafacil.domain.port.out.patient.MedicalHistoryRepositoryPort;
import com.consultafacil.domain.repository.patient.MedicalHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MedicalHistoryRepositoryAdapter implements MedicalHistoryRepositoryPort {

    private final MedicalHistoryRepository medicalHistoryRepository;

    @Override
    public MedicalHistory save(MedicalHistory history) {
        return medicalHistoryRepository.save(history);
    }

    @Override
    public Optional<MedicalHistory> findByAppointmentId(String appointmentId) {
        return medicalHistoryRepository.findByAppointmentId(appointmentId);
    }
}
