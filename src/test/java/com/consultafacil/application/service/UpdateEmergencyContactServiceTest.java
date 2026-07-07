package com.consultafacil.application.service;

import com.consultafacil.api.dto.patient.EmergencyContactDTO;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateEmergencyContactServiceTest {

    @Mock PatientProfileRepositoryPort patientProfileRepository;
    @Mock EmergencyContactRepositoryPort emergencyContactRepository;

    UpdateEmergencyContactService service;

    @BeforeEach
    void setUp() {
        service = new UpdateEmergencyContactService(
                new PatientProfileFinder(patientProfileRepository),
                new EmergencyContactFinder(emergencyContactRepository),
                new PatientOwnershipGuard(),
                emergencyContactRepository,
                new EmergencyContactMapper());
    }

    private PatientProfile profile(String profileId, String userId) {
        User user = User.builder().id(userId).build();
        return PatientProfile.builder().id(profileId).user(user).build();
    }

    @Test
    void updateEmergencyContact_wrongOwner_throwsForbidden() {
        PatientProfile owner = profile("pat-1", "user-1");
        PatientProfile requestor = profile("pat-2", "user-2");
        EmergencyContact contact = EmergencyContact.builder()
                .id("ec-1").name("Carlos").phone("11777777777").patientProfile(owner).build();

        when(patientProfileRepository.findByUserId("user-2")).thenReturn(Optional.of(requestor));
        when(emergencyContactRepository.findById("ec-1")).thenReturn(Optional.of(contact));

        EmergencyContactDTO dto = new EmergencyContactDTO(null, "Carlos Updated", "11777777777", null, null);
        assertThatThrownBy(() -> service.execute("user-2", "ec-1", dto))
                .isInstanceOf(ResponseStatusException.class);
    }
}
