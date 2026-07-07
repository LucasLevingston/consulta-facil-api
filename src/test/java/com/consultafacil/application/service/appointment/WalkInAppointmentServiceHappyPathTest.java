package com.consultafacil.application.service.appointment;

import com.consultafacil.api.dto.appointment.CreateWalkInAppointmentDTO;
import com.consultafacil.api.dto.appointment.WalkInAppointmentResponseDTO;
import com.consultafacil.api.dto.appointment.WalkInClinicalNoteDTO;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.AppointmentSource;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class WalkInAppointmentServiceHappyPathTest extends WalkInAppointmentServiceTestBase {

    @Test
    void create_withRegisteredPatient_admin_succeeds() {
        User admin = User.builder().id("adm-1").role(UserRole.ADMIN).build();
        when(userRepository.findById("adm-1")).thenReturn(Optional.of(admin));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));
        when(patientProfileRepository.findById("patprof-1")).thenReturn(Optional.of(patient));

        CreateWalkInAppointmentDTO dto = buildDTO("patprof-1", null, null);

        WalkInAppointmentResponseDTO result = service.create("adm-1", dto);

        assertThat(result.getId()).isEqualTo("appt-1");
        assertThat(result.getSource()).isEqualTo(AppointmentSource.WALK_IN);
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        verify(appointmentRepository).save(any());
    }

    @Test
    void create_withClinicalNote_savesNoteInSameTransaction() {
        User admin = User.builder().id("adm-1").role(UserRole.ADMIN).build();
        when(userRepository.findById("adm-1")).thenReturn(Optional.of(admin));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));
        when(patientProfileRepository.findById("patprof-1")).thenReturn(Optional.of(patient));

        CreateWalkInAppointmentDTO dto = buildDTO("patprof-1", null, null);
        WalkInClinicalNoteDTO noteDto = new WalkInClinicalNoteDTO();
        noteDto.setClinicalNotes("Paciente estável");
        noteDto.setDiagnosis("Hipertensão");
        dto.setClinicalNote(noteDto);

        WalkInAppointmentResponseDTO result = service.create("adm-1", dto);

        verify(clinicalNoteRepository).save(any());
        assertThat(result.getClinicalNoteId()).isEqualTo("note-1");
    }

    @Test
    void create_noClinicalNote_doesNotSaveNote() {
        User admin = User.builder().id("adm-1").role(UserRole.ADMIN).build();
        when(userRepository.findById("adm-1")).thenReturn(Optional.of(admin));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));
        when(patientProfileRepository.findById("patprof-1")).thenReturn(Optional.of(patient));

        WalkInAppointmentResponseDTO result = service.create("adm-1", buildDTO("patprof-1", null, null));

        verifyNoInteractions(clinicalNoteRepository);
        assertThat(result.getClinicalNoteId()).isNull();
    }
}
