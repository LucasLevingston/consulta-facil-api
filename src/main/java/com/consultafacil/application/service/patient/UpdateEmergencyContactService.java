package com.consultafacil.application.service.patient;

import com.consultafacil.api.dto.patient.EmergencyContactDTO;
import com.consultafacil.application.port.in.UpdateEmergencyContactUseCase;
import com.consultafacil.domain.entity.EmergencyContact;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.port.out.EmergencyContactRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateEmergencyContactService implements UpdateEmergencyContactUseCase {

    private final PatientProfileFinder profileFinder;
    private final EmergencyContactFinder contactFinder;
    private final PatientOwnershipGuard ownershipGuard;
    private final EmergencyContactRepositoryPort emergencyContactRepository;
    private final EmergencyContactMapper mapper;

    @Override
    @Transactional
    public EmergencyContactDTO execute(String userId, String contactId, EmergencyContactDTO dto) {
        PatientProfile profile = profileFinder.findOrThrow(userId);
        EmergencyContact contact = contactFinder.findOrThrow(contactId);
        ownershipGuard.assertOwnership(contact.getPatientProfile().getId(), profile.getId());
        contact.setName(dto.name());
        contact.setPhone(dto.phone());
        contact.setEmail(dto.email());
        contact.setRelationship(dto.relationship());
        return mapper.toDTO(emergencyContactRepository.save(contact));
    }
}
