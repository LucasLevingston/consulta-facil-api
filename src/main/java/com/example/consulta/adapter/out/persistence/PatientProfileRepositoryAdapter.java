package com.example.consulta.adapter.out.persistence;

import com.example.consulta.domain.entity.PatientProfile;
import com.example.consulta.domain.port.out.PatientProfileRepositoryPort;
import com.example.consulta.domain.repository.PatientProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PatientProfileRepositoryAdapter implements PatientProfileRepositoryPort {

    private final PatientProfileRepository patientProfileRepository;

    @Override
    public PatientProfile save(PatientProfile profile) {
        return patientProfileRepository.save(profile);
    }

    @Override
    public Optional<PatientProfile> findById(String id) {
        return patientProfileRepository.findById(id);
    }

    @Override
    public Optional<PatientProfile> findByUserId(String userId) {
        return patientProfileRepository.findByUserId(userId);
    }
}
