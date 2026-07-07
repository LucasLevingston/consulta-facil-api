package com.consultafacil.application.service;

import com.consultafacil.api.dto.patient.EmergencyContactDTO;
import com.consultafacil.application.port.in.AddEmergencyContactUseCase;
import com.consultafacil.domain.entity.EmergencyContact;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.port.out.EmergencyContactRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddEmergencyContactService implements AddEmergencyContactUseCase {

    private final PatientProfileFinder profileFinder;
    private final EmergencyContactRepositoryPort emergencyContactRepository;
    private final EmergencyContactMapper mapper;

    @Override
    @Transactional
    public EmergencyContactDTO execute(String userId, EmergencyContactDTO dto) {
        PatientProfile profile = profileFinder.findOrThrow(userId);
        EmergencyContact contact = EmergencyContact.builder()
                .name(dto.name())
                .phone(dto.phone())
                .email(dto.email())
                .relationship(dto.relationship())
                .patientProfile(profile)
                .build();
        return mapper.toDTO(emergencyContactRepository.save(contact));
    }
}
