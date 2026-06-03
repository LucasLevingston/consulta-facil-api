package com.example.consulta.application.service;

import com.example.consulta.application.port.in.command.RescheduleAppointmentCommand;
import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.core.security.OwnershipValidator;
import com.example.consulta.domain.entity.*;
import com.example.consulta.domain.enums.*;
import com.example.consulta.domain.port.out.AppointmentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RescheduleAppointmentServiceTest {

    @Mock AppointmentRepositoryPort appointmentRepository;
    @Mock OwnershipValidator ownershipValidator;
    @InjectMocks RescheduleAppointmentService service;

    Appointment appointment;
    LocalDateTime newDate = LocalDateTime.now().plusDays(5);

    @BeforeEach
    void setUp() {
        User pUser = User.builder().id("u-1").email("p@e.com").name("João").password("x").role(UserRole.PATIENT).build();
        PatientProfile patient = new PatientProfile(); patient.setId("pp-1"); patient.setUser(pUser);
        User dUser = User.builder().id("u-2").email("d@e.com").name("Dra.Ana").password("x").role(UserRole.PROFESSIONAL).build();
        ProfessionalProfile prof = new ProfessionalProfile(); prof.setId("pr-1"); prof.setUser(dUser); prof.setSpecialty("Cardio");

        appointment = Appointment.schedule(patient, prof, LocalDateTime.now().plusDays(1),
                "Consulta", null, AppointmentModality.IN_PERSON, null, null, null);
        appointment.setId("appt-1");

        doNothing().when(ownershipValidator).verifyAppointmentAccess(any(), any());
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test void execute_validReschedule_updatesDate() {
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        when(appointmentRepository.existsByProfessionalIdAndScheduledAt(any(), any())).thenReturn(false);

        var result = service.execute(new RescheduleAppointmentCommand("appt-1", "u-1", newDate, "Novo motivo"));

        assertThat(result.getScheduledAt()).isEqualTo(newDate);
    }

    @Test void execute_conflictingSlot_throwsBadRequest() {
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        when(appointmentRepository.existsByProfessionalIdAndScheduledAt(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> service.execute(new RescheduleAppointmentCommand("appt-1", "u-1", newDate, null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already has an appointment");
    }

    @Test void execute_notFound_throwsResourceNotFound() {
        when(appointmentRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new RescheduleAppointmentCommand("bad", "u-1", newDate, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
