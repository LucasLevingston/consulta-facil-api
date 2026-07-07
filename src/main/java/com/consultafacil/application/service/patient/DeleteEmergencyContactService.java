package com.consultafacil.application.service.patient;

import com.consultafacil.application.port.in.DeleteEmergencyContactUseCase;
import com.consultafacil.domain.entity.EmergencyContact;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.port.out.EmergencyContactRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteEmergencyContactService implements DeleteEmergencyContactUseCase {

    private final PatientProfileFinder profileFinder;
    private final EmergencyContactFinder contactFinder;
    private final PatientOwnershipGuard ownershipGuard;
    private final EmergencyContactRepositoryPort emergencyContactRepository;

    @Override
    @Transactional
    public void execute(String userId, String contactId) {
        PatientProfile profile = profileFinder.findOrThrow(userId);
        EmergencyContact contact = contactFinder.findOrThrow(contactId);
        ownershipGuard.assertOwnership(contact.getPatientProfile().getId(), profile.getId());
        emergencyContactRepository.delete(contact);
    }
}
