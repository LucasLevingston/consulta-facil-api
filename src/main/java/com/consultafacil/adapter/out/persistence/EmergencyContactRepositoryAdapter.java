package com.consultafacil.adapter.out.persistence;

import com.consultafacil.domain.entity.EmergencyContact;
import com.consultafacil.domain.port.out.EmergencyContactRepositoryPort;
import com.consultafacil.domain.repository.EmergencyContactRepository;
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
