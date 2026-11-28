package com.consultafacil.application.service.professional.profile;

import com.consultafacil.api.dto.appointment.PatientSummaryDTO;
import com.consultafacil.domain.PatientSummary;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetProfessionalPatientsServiceTest {

    @Mock AppointmentRepositoryPort appointmentRepository;

    @InjectMocks GetProfessionalPatientsService service;

    private PatientSummary patientSummary;

    @BeforeEach
    void setUp() {
        patientSummary = new PatientSummary(
                "patient-1",
                "Maria Silva",
                LocalDateTime.of(2024, 5, 10, 9, 0),
                3L);
    }

    @Test
    void execute_sortByRecent_returnsPatients() {
        when(appointmentRepository.findProfessionalPatientsByRecent(eq("prof-1"), eq(""), any()))
                .thenReturn(new PageImpl<>(List.of(patientSummary)));

        Page<PatientSummaryDTO> result = service.execute("prof-1", null, "recent", 0, 10);

        assertThat(result.getTotalElements()).isEqualTo(1);
        PatientSummaryDTO dto = result.getContent().get(0);
        assertThat(dto.getId()).isEqualTo("patient-1");
        assertThat(dto.getName()).isEqualTo("Maria Silva");
        assertThat(dto.getTotalAppointments()).isEqualTo(3L);
    }

    @Test
    void execute_sortByName_returnsPatients() {
        when(appointmentRepository.findProfessionalPatientsByName(eq("prof-1"), eq("Maria"), any()))
                .thenReturn(new PageImpl<>(List.of(patientSummary)));

        Page<PatientSummaryDTO> result = service.execute("prof-1", "Maria", "name", 0, 10);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Maria Silva");
    }

    @Test
    void execute_emptyResult_returnsEmptyPage() {
        when(appointmentRepository.findProfessionalPatientsByRecent(eq("prof-99"), eq(""), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<PatientSummaryDTO> result = service.execute("prof-99", null, "recent", 0, 10);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void execute_mapsLastAppointment() {
        LocalDateTime expected = LocalDateTime.of(2024, 5, 10, 9, 0);
        when(appointmentRepository.findProfessionalPatientsByRecent(eq("prof-1"), eq(""), any()))
                .thenReturn(new PageImpl<>(List.of(patientSummary)));

        Page<PatientSummaryDTO> result = service.execute("prof-1", null, "recent", 0, 10);

        assertThat(result.getContent().get(0).getLastAppointment()).isEqualTo(expected);
    }
}
