package com.consultafacil.adapter.out.persistence;

import com.consultafacil.domain.entity.EmergencyContact;
import com.consultafacil.domain.port.out.EmergencyContactRepositoryPort;
import com.consultafacil.domain.repository.EmergencyContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
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
    public List<EmergencyContact> findByPatientProfileId(String patientProfileId) {
        return emergencyContactRepository.findByPatientProfileId(patientProfileId);
    }

    @Override
    public Optional<EmergencyContact> findById(String id) {
        return emergencyContactRepository.findById(id);
    }

    @Override
    public void delete(EmergencyContact contact) {
        emergencyContactRepository.delete(contact);
    }
}
