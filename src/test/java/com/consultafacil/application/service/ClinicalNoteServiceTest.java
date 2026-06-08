package com.consultafacil.application.service;

import com.consultafacil.api.dto.appointment.SaveClinicalNoteDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.core.security.OwnershipValidator;
import com.consultafacil.domain.entity.*;
import com.consultafacil.domain.enums.*;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.ClinicalNoteRepositoryPort;
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
class ClinicalNoteServiceTest {

    @Mock ClinicalNoteRepositoryPort clinicalNoteRepository;
    @Mock AppointmentRepositoryPort appointmentRepository;
    @Mock OwnershipValidator ownershipValidator;

    @InjectMocks ClinicalNoteService service;

    Appointment appointment;
    ClinicalNote note;

    @BeforeEach
    void setUp() {
        User pUser = User.builder().id("u-1").email("p@e.com").name("João").password("x").role(UserRole.PATIENT).build();
        PatientProfile patient = new PatientProfile(); patient.setId("pp-1"); patient.setUser(pUser);
        User dUser = User.builder().id("u-2").email("d@e.com").name("Dra.").password("x").role(UserRole.PROFESSIONAL).build();
        ProfessionalProfile prof = new ProfessionalProfile(); prof.setId("pr-1"); prof.setUser(dUser); prof.setSpecialty("C");

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
        doNothing().when(ownershipValidator).verifyProfessionalOwnership(any(), any());
        when(clinicalNoteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void getByAppointmentId_found_returnsDTO() {
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        when(clinicalNoteRepository.findByAppointmentId("appt-1")).thenReturn(Optional.of(note));

        var result = service.getByAppointmentId("appt-1", "u-2");

        assertThat(result).isPresent();
        assertThat(result.get().getDiagnosis()).isEqualTo("Hipertensão");
    }

    @Test
    void getByAppointmentId_noNote_returnsEmpty() {
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        when(clinicalNoteRepository.findByAppointmentId("appt-1")).thenReturn(Optional.empty());

        var result = service.getByAppointmentId("appt-1", "u-2");

        assertThat(result).isEmpty();
    }

    @Test
    void getByAppointmentId_appointmentNotFound_throwsNotFound() {
        when(appointmentRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByAppointmentId("bad", "u-2"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void save_newNote_createsClinicalNote() {
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        when(clinicalNoteRepository.findByAppointmentId("appt-1")).thenReturn(Optional.empty());

        var dto = SaveClinicalNoteDTO.builder()
                .clinicalNotes("Paciente hipertenso")
                .diagnosis("I10")
                .prescription("Losartana 50mg")
                .build();

        var result = service.save("appt-1", "u-2", dto);

        assertThat(result.getClinicalNotes()).isEqualTo("Paciente hipertenso");
        verify(clinicalNoteRepository).save(any());
    }

    @Test
    void save_existingNote_updatesFields() {
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        when(clinicalNoteRepository.findByAppointmentId("appt-1")).thenReturn(Optional.of(note));

        var dto = SaveClinicalNoteDTO.builder().clinicalNotes("Atualizado").build();

        service.save("appt-1", "u-2", dto);

        verify(clinicalNoteRepository).save(note);
        assertThat(note.getClinicalNotes()).isEqualTo("Atualizado");
    }

    @Test
    void save_appointmentNotFound_throwsNotFound() {
        when(appointmentRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.save("bad", "u-2", new SaveClinicalNoteDTO()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
