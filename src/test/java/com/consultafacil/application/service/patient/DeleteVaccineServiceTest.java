package com.consultafacil.application.service.patient;

import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.PatientVaccine;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.patient.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.patient.PatientVaccineRepositoryPort;
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
class DeleteVaccineServiceTest {

    @Mock PatientProfileRepositoryPort patientProfileRepository;
    @Mock PatientVaccineRepositoryPort vaccineRepository;

    DeleteVaccineService service;

    @BeforeEach
    void setUp() {
        service = new DeleteVaccineService(
                new PatientProfileFinder(patientProfileRepository), new PatientOwnershipGuard(), vaccineRepository);
    }

    private PatientProfile profile(String profileId, String userId) {
        User user = User.builder().id(userId).build();
        return PatientProfile.builder().id(profileId).user(user).build();
    }

    @Test
    void deleteVaccine_wrongOwner_throwsForbidden() {
        PatientProfile owner = profile("pat-1", "user-1");
        PatientProfile requestor = profile("pat-2", "user-2");
        PatientVaccine vaccine = PatientVaccine.builder()
                .id("vac-1").vaccineName("Flu").patientProfile(owner).build();

        when(patientProfileRepository.findByUserId("user-2")).thenReturn(Optional.of(requestor));
        when(vaccineRepository.findById("vac-1")).thenReturn(Optional.of(vaccine));

        assertThatThrownBy(() -> service.execute("user-2", "vac-1"))
                .isInstanceOf(ResponseStatusException.class);
    }
}
