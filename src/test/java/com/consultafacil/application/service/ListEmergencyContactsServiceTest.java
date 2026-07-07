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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListEmergencyContactsServiceTest {

    @Mock PatientProfileRepositoryPort patientProfileRepository;
    @Mock EmergencyContactRepositoryPort emergencyContactRepository;

    ListEmergencyContactsService service;

    @BeforeEach
    void setUp() {
        service = new ListEmergencyContactsService(
                new PatientProfileFinder(patientProfileRepository), emergencyContactRepository, new EmergencyContactMapper());
    }

    private PatientProfile profile(String profileId, String userId) {
        User user = User.builder().id(userId).build();
        return PatientProfile.builder().id(profileId).user(user).build();
    }

    @Test
    void listEmergencyContacts_returnsAll() {
        PatientProfile p = profile("pat-1", "user-1");
        EmergencyContact c1 = EmergencyContact.builder().id("ec-1").name("A").phone("111").patientProfile(p).build();
        EmergencyContact c2 = EmergencyContact.builder().id("ec-2").name("B").phone("222").patientProfile(p).build();

        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(p));
        when(emergencyContactRepository.findByPatientProfileId("pat-1")).thenReturn(List.of(c1, c2));

        List<EmergencyContactDTO> result = service.execute("user-1");

        assertThat(result).hasSize(2);
    }
}
