package com.consultafacil.application.service;

import com.consultafacil.application.observability.BusinessMetrics;
import com.consultafacil.application.port.in.command.CancelAppointmentCommand;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.core.security.OwnershipValidator;
import com.consultafacil.domain.entity.*;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.AppointmentNotificationPort;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CancelAppointmentServiceTest {

    @Mock AppointmentRepositoryPort appointmentRepository;
    @Mock AppointmentNotificationPort appointmentNotification;
    @Mock OwnershipValidator ownershipValidator;
    @Mock BusinessMetrics businessMetrics;

    CancelAppointmentService service;
    Appointment appointment;

    @BeforeEach
    void setUp() {
        User patientUser = User.builder().id("u-1").email("p@email.com").name("João").password("x").role(UserRole.PATIENT).build();
        PatientProfile patient = new PatientProfile();
        patient.setId("pp-1");
        patient.setUser(patientUser);

        User profUser = User.builder().id("u-2").email("dr@email.com").name("Dra. Ana").password("x").role(UserRole.PROFESSIONAL).build();
        ProfessionalProfile professional = new ProfessionalProfile();
        professional.setId("prof-1");
        professional.setUser(profUser);
        professional.setSpecialty(Specialty.CARDIOLOGIA);

        appointment = new Appointment();
        appointment.setId("appt-1");
        appointment.setPatient(patient);
        appointment.setProfessional(professional);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setScheduledAt(LocalDateTime.now().plusDays(5));

        service = new CancelAppointmentService(appointmentRepository, appointmentNotification,
                ownershipValidator, businessMetrics, new AppointmentMapper());

        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(ownershipValidator).verifyAppointmentAccess(any(), any());
    }

    @Test
    void cancel_validAppointment_setsStatusCanceled() {
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));

        service.execute(new CancelAppointmentCommand("appt-1", "u-1", "Conflito de agenda"));

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELED);
        assertThat(appointment.getCancellationReason()).isEqualTo("Conflito de agenda");
    }

    @Test
    void cancel_notFound_throwsNotFound() {
        when(appointmentRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new CancelAppointmentCommand("bad", "u-1", "reason")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancel_publishesNotification() {
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));

        service.execute(new CancelAppointmentCommand("appt-1", "u-1", "reason"));

        verify(appointmentNotification).notifyCanceled(any());
        verify(businessMetrics).recordAppointmentCanceled();
    }
}
