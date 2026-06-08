package com.consultafacil.application.service;

import com.consultafacil.api.dto.appointment.SaveMedicalHistoryDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.core.security.OwnershipValidator;
import com.consultafacil.domain.entity.*;
import com.consultafacil.domain.enums.*;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.MedicalHistoryRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MedicalHistoryServiceTest {

    @Mock MedicalHistoryRepositoryPort medicalHistoryRepository;
    @Mock AppointmentRepositoryPort appointmentRepository;
    @Mock OwnershipValidator ownershipValidator;

    @InjectMocks MedicalHistoryService service;

    Appointment appointment;

    @BeforeEach
    void setUp() {
        User pUser = User.builder().id("u-1").email("p@e.com").name("João").password("x").role(UserRole.PATIENT).build();
        PatientProfile patient = new PatientProfile(); patient.setId("pp-1"); patient.setUser(pUser);
        User dUser = User.builder().id("u-2").email("d@e.com").name("Dra.").password("x").role(UserRole.PROFESSIONAL).build();
        ProfessionalProfile prof = new ProfessionalProfile(); prof.setId("pr-1"); prof.setUser(dUser); prof.setSpecialty("C");

        appointment = new Appointment();
        appointment.setId("appt-1"); appointment.setPatient(patient); appointment.setProfessional(prof);
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        doNothing().when(ownershipValidator).verifyAppointmentAccess(any(), any());
        when(medicalHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void getByAppointmentId_found_returnsDTO() {
        MedicalHistory history = MedicalHistory.builder().id("h-1").appointment(appointment).chiefComplaint("Dor").build();
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        when(medicalHistoryRepository.findByAppointmentId("appt-1")).thenReturn(Optional.of(history));

        var result = service.getByAppointmentId("appt-1", "u-1");

        assertThat(result).isPresent();
        assertThat(result.get().getChiefComplaint()).isEqualTo("Dor");
    }

    @Test
    void getByAppointmentId_noHistory_returnsEmpty() {
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        when(medicalHistoryRepository.findByAppointmentId("appt-1")).thenReturn(Optional.empty());

        assertThat(service.getByAppointmentId("appt-1", "u-1")).isEmpty();
    }

    @Test
    void getByAppointmentId_notFound_throwsResourceNotFound() {
        when(appointmentRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByAppointmentId("bad", "u-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void save_newHistory_creates() {
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        when(medicalHistoryRepository.findByAppointmentId("appt-1")).thenReturn(Optional.empty());

        var dto = SaveMedicalHistoryDTO.builder().chiefComplaint("Febre").allergies("Penicilina").build();
        var result = service.save("appt-1", "u-1", dto);

        assertThat(result.getChiefComplaint()).isEqualTo("Febre");
        verify(medicalHistoryRepository).save(any());
    }

    @Test
    void save_appointmentNotFound_throwsNotFound() {
        when(appointmentRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.save("bad", "u-1", new SaveMedicalHistoryDTO()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
