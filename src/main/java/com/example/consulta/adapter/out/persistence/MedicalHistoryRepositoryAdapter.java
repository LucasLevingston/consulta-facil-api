package com.example.consulta.adapter.out.persistence;

import com.example.consulta.domain.entity.MedicalHistory;
import com.example.consulta.domain.port.out.MedicalHistoryRepositoryPort;
import com.example.consulta.domain.repository.MedicalHistoryRepository;
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
