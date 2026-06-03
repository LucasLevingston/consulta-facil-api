package com.example.consulta.adapter.out.persistence;

import com.example.consulta.domain.entity.MedicalRecord;
import com.example.consulta.domain.port.out.MedicalRecordRepositoryPort;
import com.example.consulta.domain.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MedicalRecordRepositoryAdapter implements MedicalRecordRepositoryPort {

    private final MedicalRecordRepository medicalRecordRepository;

    @Override
    public MedicalRecord save(MedicalRecord record) {
        return medicalRecordRepository.save(record);
    }

    @Override
    public Optional<MedicalRecord> findByPatientProfileId(String patientProfileId) {
        return medicalRecordRepository.findByPatientProfileId(patientProfileId);
    }
}
