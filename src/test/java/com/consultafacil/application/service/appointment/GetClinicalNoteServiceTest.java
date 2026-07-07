package com.consultafacil.application.service.appointment;

import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.core.security.OwnershipValidator;
import com.consultafacil.domain.entity.*;
import com.consultafacil.domain.enums.*;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.appointment.ClinicalNoteRepositoryPort;
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
class GetClinicalNoteServiceTest {

    @Mock ClinicalNoteRepositoryPort clinicalNoteRepository;
    @Mock AppointmentRepositoryPort appointmentRepository;
    @Mock OwnershipValidator ownershipValidator;

    @InjectMocks GetClinicalNoteService service;

    Appointment appointment;
    ClinicalNote note;

    @BeforeEach
    void setUp() {
        User pUser = User.builder().id("u-1").email("p@e.com").name("João").password("x").role(UserRole.PATIENT).build();
        PatientProfile patient = new PatientProfile(); patient.setId("pp-1"); patient.setUser(pUser);
        User dUser = User.builder().id("u-2").email("d@e.com").name("Dra.").password("x").role(UserRole.PROFESSIONAL).build();
        ProfessionalProfile prof = new ProfessionalProfile(); prof.setId("pr-1"); prof.setUser(dUser); prof.setSpecialty(Specialty.CARDIOLOGIA);

        appointment = new Appointment();
        appointment.setId("appt-1");
        appointment.setPatient(patient);
        appointment.setProfessional(prof);
        appointment.setStatus(AppointmentStatus.COMPLETED);

        note = ClinicalNote.builder()
                .id("note-1")
                .appointment(appointment)
                .clinicalNotes("Paciente estável")
                .diagnosis("Hipertensão")
                .build();

        doNothing().when(ownershipValidator).verifyAppointmentAccess(any(), any());
    }

    @Test
    void execute_found_returnsDTO() {
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        when(clinicalNoteRepository.findByAppointmentId("appt-1")).thenReturn(Optional.of(note));

        var result = service.execute("appt-1", "u-2");

        assertThat(result).isPresent();
        assertThat(result.get().getDiagnosis()).isEqualTo("Hipertensão");
    }

    @Test
    void execute_noNote_returnsEmpty() {
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        when(clinicalNoteRepository.findByAppointmentId("appt-1")).thenReturn(Optional.empty());

        var result = service.execute("appt-1", "u-2");

        assertThat(result).isEmpty();
    }

    @Test
    void execute_appointmentNotFound_throwsNotFound() {
        when(appointmentRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("bad", "u-2"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
