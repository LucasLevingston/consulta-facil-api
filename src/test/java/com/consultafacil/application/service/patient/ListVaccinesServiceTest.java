package com.consultafacil.application.service.patient;

import com.consultafacil.api.dto.patient.PatientVaccineDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListVaccinesServiceTest {

    @Mock PatientProfileRepositoryPort patientProfileRepository;
    @Mock PatientVaccineRepositoryPort vaccineRepository;

    ListVaccinesService service;

    PatientProfile profile;

    @BeforeEach
    void setUp() {
        User user = User.builder().id("user-1").name("Ana").email("ana@email.com").build();
        profile = PatientProfile.builder().id("pat-1").user(user).build();

        service = new ListVaccinesService(
                new PatientProfileFinder(patientProfileRepository),
                vaccineRepository,
                new PatientVaccineMapper());
    }

    @Test
    void execute_profileNotFound_throwsResourceNotFoundException() {
        when(patientProfileRepository.findByUserId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_noVaccines_returnsEmptyList() {
        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(profile));
        when(vaccineRepository.findByPatientProfileId("pat-1")).thenReturn(List.of());

        List<PatientVaccineDTO> result = service.execute("user-1");

        assertThat(result).isEmpty();
    }

    @Test
    void execute_withVaccines_returnsMappedList() {
        PatientVaccine v1 = PatientVaccine.builder()
                .id("vac-1")
                .patientProfile(profile)
                .vaccineName("COVID-19")
                .doseNumber("1a dose")
                .administeredAt(LocalDate.of(2021, 5, 15))
                .notes("No side effects")
                .build();
        PatientVaccine v2 = PatientVaccine.builder()
                .id("vac-2")
                .patientProfile(profile)
                .vaccineName("Influenza")
                .doseNumber("dose unica")
                .administeredAt(LocalDate.of(2023, 3, 10))
                .build();

        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(profile));
        when(vaccineRepository.findByPatientProfileId("pat-1")).thenReturn(List.of(v1, v2));

        List<PatientVaccineDTO> result = service.execute("user-1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo("vac-1");
        assertThat(result.get(0).vaccineName()).isEqualTo("COVID-19");
        assertThat(result.get(0).administeredAt()).isEqualTo(LocalDate.of(2021, 5, 15));
        assertThat(result.get(1).vaccineName()).isEqualTo("Influenza");
    }

    @Test
    void execute_vaccineWithNullNotes_mappedCorrectly() {
        PatientVaccine v = PatientVaccine.builder()
                .id("vac-3")
                .patientProfile(profile)
                .vaccineName("Hepatite B")
                .doseNumber("2a dose")
                .build();

        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(profile));
        when(vaccineRepository.findByPatientProfileId("pat-1")).thenReturn(List.of(v));

        List<PatientVaccineDTO> result = service.execute("user-1");

        assertThat(result.get(0).notes()).isNull();
        assertThat(result.get(0).administeredAt()).isNull();
    }
}
