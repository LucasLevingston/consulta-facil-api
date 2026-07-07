package com.consultafacil.application.service;

import com.consultafacil.api.dto.patient.EmergencyContactDTO;
import com.consultafacil.application.port.in.ListEmergencyContactsUseCase;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.port.out.EmergencyContactRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListEmergencyContactsService implements ListEmergencyContactsUseCase {

    private final PatientProfileFinder profileFinder;
    private final EmergencyContactRepositoryPort emergencyContactRepository;
    private final EmergencyContactMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<EmergencyContactDTO> execute(String userId) {
        PatientProfile profile = profileFinder.findOrThrow(userId);
        return emergencyContactRepository.findByPatientProfileId(profile.getId())
                .stream().map(mapper::toDTO).toList();
    }
}
