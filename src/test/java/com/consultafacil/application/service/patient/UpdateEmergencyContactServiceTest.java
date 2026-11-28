package com.consultafacil.application.service.patient;

import com.consultafacil.api.dto.patient.EmergencyContactDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.EmergencyContact;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.EmergencyContactRelationship;
import com.consultafacil.domain.port.out.patient.EmergencyContactRepositoryPort;
import com.consultafacil.domain.port.out.patient.PatientProfileRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
    void execute_profileNotFound_throwsResourceNotFoundException() {
        when(patientProfileRepository.findByUserId("missing")).thenReturn(Optional.empty());

        EmergencyContactDTO dto = new EmergencyContactDTO(null, "X", "11999999999", null, null);
        assertThatThrownBy(() -> service.execute("missing", "ec-1", dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_contactNotFound_throwsResourceNotFoundException() {
        PatientProfile requestor = profile("pat-1", "user-1");
        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(requestor));
        when(emergencyContactRepository.findById("missing")).thenReturn(Optional.empty());

        EmergencyContactDTO dto = new EmergencyContactDTO(null, "X", "11999999999", null, null);
        assertThatThrownBy(() -> service.execute("user-1", "missing", dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_validOwner_updatesContactFields() {
        PatientProfile owner = profile("pat-1", "user-1");
        EmergencyContact contact = EmergencyContact.builder()
                .id("ec-1").name("Carlos").phone("11777777777")
                .email("old@email.com")
                .relationship(EmergencyContactRelationship.FRIEND)
                .patientProfile(owner)
                .build();

        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(owner));
        when(emergencyContactRepository.findById("ec-1")).thenReturn(Optional.of(contact));
        when(emergencyContactRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EmergencyContactDTO dto = new EmergencyContactDTO(
                null, "Carlos Updated", "11888888888", "new@email.com", EmergencyContactRelationship.SPOUSE);
        EmergencyContactDTO result = service.execute("user-1", "ec-1", dto);

        assertThat(result.name()).isEqualTo("Carlos Updated");
        assertThat(result.phone()).isEqualTo("11888888888");
        assertThat(result.email()).isEqualTo("new@email.com");
        assertThat(result.relationship()).isEqualTo(EmergencyContactRelationship.SPOUSE);
    }

    @Test
    void execute_validOwner_savesContact() {
        PatientProfile owner = profile("pat-1", "user-1");
        EmergencyContact contact = EmergencyContact.builder()
                .id("ec-1").name("Maria").phone("11999999999").patientProfile(owner).build();

        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(owner));
        when(emergencyContactRepository.findById("ec-1")).thenReturn(Optional.of(contact));
        when(emergencyContactRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EmergencyContactDTO dto = new EmergencyContactDTO(null, "Maria Updated", "11000000000", null, null);
        service.execute("user-1", "ec-1", dto);

        verify(emergencyContactRepository).save(contact);
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
