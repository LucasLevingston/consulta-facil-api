package com.example.consulta.adapter.out.persistence;

import com.example.consulta.domain.entity.EmergencyContact;
import com.example.consulta.domain.port.out.EmergencyContactRepositoryPort;
import com.example.consulta.domain.repository.EmergencyContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmergencyContactRepositoryAdapter implements EmergencyContactRepositoryPort {

    private final EmergencyContactRepository emergencyContactRepository;

    @Override
    public EmergencyContact save(EmergencyContact contact) {
        return emergencyContactRepository.save(contact);
    }

    @Override
    public Optional<EmergencyContact> findByPatientProfileId(String patientProfileId) {
        return emergencyContactRepository.findByPatientProfileId(patientProfileId);
    }
}
