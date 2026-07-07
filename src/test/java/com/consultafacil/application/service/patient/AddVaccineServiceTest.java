package com.consultafacil.application.service.patient;

import com.consultafacil.api.dto.patient.PatientVaccineDTO;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddVaccineServiceTest {

    @Mock PatientProfileRepositoryPort patientProfileRepository;
    @Mock PatientVaccineRepositoryPort vaccineRepository;

    AddVaccineService service;

    @BeforeEach
    void setUp() {
        service = new AddVaccineService(
                new PatientProfileFinder(patientProfileRepository), vaccineRepository, new PatientVaccineMapper());
    }

    private PatientProfile profile(String profileId, String userId) {
        User user = User.builder().id(userId).build();
        return PatientProfile.builder().id(profileId).user(user).build();
    }

    @Test
    void addVaccine_valid_savesAndReturns() {
        PatientProfile p = profile("pat-1", "user-1");
        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(p));
        when(vaccineRepository.save(any())).thenAnswer(inv -> {
            PatientVaccine v = inv.getArgument(0);
            return PatientVaccine.builder()
                    .id("vac-1").vaccineName(v.getVaccineName())
                    .doseNumber(v.getDoseNumber()).patientProfile(p).build();
        });

        PatientVaccineDTO dto = new PatientVaccineDTO(null, "COVID-19", "2ª dose", null, null);
        PatientVaccineDTO result = service.execute("user-1", dto);

        assertThat(result.id()).isEqualTo("vac-1");
        assertThat(result.vaccineName()).isEqualTo("COVID-19");
    }
}
