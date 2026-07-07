package com.consultafacil.application.service;

import com.consultafacil.domain.entity.EmergencyContact;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.EmergencyContactRepositoryPort;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteEmergencyContactServiceTest {

    @Mock PatientProfileRepositoryPort patientProfileRepository;
    @Mock EmergencyContactRepositoryPort emergencyContactRepository;

    DeleteEmergencyContactService service;

    @BeforeEach
    void setUp() {
        service = new DeleteEmergencyContactService(
                new PatientProfileFinder(patientProfileRepository),
                new EmergencyContactFinder(emergencyContactRepository),
                new PatientOwnershipGuard(),
                emergencyContactRepository);
    }

    private PatientProfile profile(String profileId, String userId) {
        User user = User.builder().id(userId).build();
        return PatientProfile.builder().id(profileId).user(user).build();
    }

    @Test
    void deleteEmergencyContact_valid_deletesEntry() {
        PatientProfile p = profile("pat-1", "user-1");
        EmergencyContact contact = EmergencyContact.builder()
                .id("ec-1").name("Ana").phone("11666666666").patientProfile(p).build();

        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(p));
        when(emergencyContactRepository.findById("ec-1")).thenReturn(Optional.of(contact));

        service.execute("user-1", "ec-1");

        verify(emergencyContactRepository).delete(contact);
    }
}
