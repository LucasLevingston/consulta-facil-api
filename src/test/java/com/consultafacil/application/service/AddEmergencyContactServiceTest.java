package com.consultafacil.application.service;

import com.consultafacil.api.dto.patient.EmergencyContactDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.EmergencyContact;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.EmergencyContactRelationship;
import com.consultafacil.domain.port.out.EmergencyContactRepositoryPort;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddEmergencyContactServiceTest {

    @Mock PatientProfileRepositoryPort patientProfileRepository;
    @Mock EmergencyContactRepositoryPort emergencyContactRepository;

    AddEmergencyContactService service;

    @BeforeEach
    void setUp() {
        service = new AddEmergencyContactService(
                new PatientProfileFinder(patientProfileRepository), emergencyContactRepository, new EmergencyContactMapper());
    }

    private PatientProfile profile(String profileId, String userId) {
        User user = User.builder().id(userId).build();
        return PatientProfile.builder().id(profileId).user(user).build();
    }

    @Test
    void addEmergencyContact_valid_savesAndReturns() {
        PatientProfile p = profile("pat-1", "user-1");
        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(p));
        when(emergencyContactRepository.save(any())).thenAnswer(inv -> {
            EmergencyContact c = inv.getArgument(0);
            return EmergencyContact.builder()
                    .id("ec-1").name(c.getName()).phone(c.getPhone())
                    .email(c.getEmail()).relationship(c.getRelationship())
                    .patientProfile(p).build();
        });

        EmergencyContactDTO dto = new EmergencyContactDTO(null, "Maria", "11999999999",
                "maria@email.com", EmergencyContactRelationship.MOTHER);
        EmergencyContactDTO result = service.execute("user-1", dto);

        assertThat(result.id()).isEqualTo("ec-1");
        assertThat(result.name()).isEqualTo("Maria");
        assertThat(result.relationship()).isEqualTo(EmergencyContactRelationship.MOTHER);
    }

    @Test
    void addEmergencyContact_profileNotFound_throwsNotFound() {
        when(patientProfileRepository.findByUserId("unknown")).thenReturn(Optional.empty());
        EmergencyContactDTO dto = new EmergencyContactDTO(null, "João", "11888888888", null, null);
        assertThatThrownBy(() -> service.execute("unknown", dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
