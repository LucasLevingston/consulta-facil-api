package com.example.consulta.application.service;

import com.example.consulta.application.observability.BusinessMetrics;
import com.example.consulta.application.port.in.command.CancelAppointmentCommand;
import com.example.consulta.application.port.in.command.RateAppointmentCommand;
import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.core.security.OwnershipValidator;
import com.example.consulta.domain.entity.*;
import com.example.consulta.domain.enums.*;
import com.example.consulta.domain.port.out.*;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppointmentServiceTest {

    @Mock AppointmentRepositoryPort appointmentRepository;
    @Mock PatientProfileRepositoryPort patientProfileRepository;
    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;
    @Mock ProfessionalServiceRepositoryPort professionalServiceRepository;
    @Mock AppointmentNotificationPort appointmentNotification;
    @Mock CreateAppointmentPaymentService createAppointmentPaymentService;
    @Mock BusinessMetrics businessMetrics;
    @Mock OwnershipValidator ownershipValidator;

    @InjectMocks AppointmentService service;

    User patientUser;
    PatientProfile patient;
    User profUser;
    ProfessionalProfile professional;
    Appointment appointment;

    @BeforeEach
    void setUp() {
        patientUser = User.builder().id("u-1").email("p@email.com").name("João").password("x").role(UserRole.PATIENT).build();
        patient = new PatientProfile();
        patient.setId("pp-1");
        patient.setUser(patientUser);

        profUser = User.builder().id("u-2").email("dr@email.com").name("Dra. Ana").password("x").role(UserRole.PROFESSIONAL).build();
        professional = new ProfessionalProfile();
        professional.setId("prof-1");
        professional.setUser(profUser);
        professional.setSpecialty("Cardiologia");

        appointment = new Appointment();
        appointment.setId("appt-1");
        appointment.setPatient(patient);
        appointment.setProfessional(professional);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setScheduledAt(LocalDateTime.now().plusDays(5));

        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(ownershipValidator).verifyAppointmentAccess(any(), any());
        doNothing().when(appointmentNotification).notifyCanceled(any());
        doNothing().when(businessMetrics).recordAppointmentCanceled();
    }

    // ── cancel ────────────────────────────────────────────────────────────

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

    // ── rate ──────────────────────────────────────────────────────────────

    @Test
    void rate_validCompletedAppointment_setsRating() {
        appointment.setStatus(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(patient));

        var result = service.execute(new RateAppointmentCommand("appt-1", "u-1", 5, "Ótimo!"));

        assertThat(appointment.getRating()).isEqualTo(5);
        assertThat(appointment.getRatingComment()).isEqualTo("Ótimo!");
    }

    @Test
    void rate_appointmentNotFound_throwsNotFound() {
        when(appointmentRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new RateAppointmentCommand("bad", "u-1", 5, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rate_wrongPatient_throwsBadRequest() {
        appointment.setStatus(AppointmentStatus.COMPLETED);
        PatientProfile otherPatient = new PatientProfile();
        otherPatient.setId("pp-other");
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        when(patientProfileRepository.findByUserId("u-other")).thenReturn(Optional.of(otherPatient));

        assertThatThrownBy(() -> service.execute(new RateAppointmentCommand("appt-1", "u-other", 5, null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("your own appointments");
    }

    @Test
    void rate_notCompleted_throwsInvalidState() {
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(patient));

        assertThatThrownBy(() -> service.execute(new RateAppointmentCommand("appt-1", "u-1", 5, null)))
                .isInstanceOf(Exception.class);
    }

    // ── delete ────────────────────────────────────────────────────────────

    @Test
    void delete_existingAppointment_callsDelete() {
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        doNothing().when(appointmentRepository).delete(appointment);

        service.delete("appt-1");

        verify(appointmentRepository).delete(appointment);
    }

    @Test
    void delete_notFound_throwsNotFound() {
        when(appointmentRepository.findById("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete("bad"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── backward-compat bridges ───────────────────────────────────────────

    @Test
    void scheduleAppointment_delegatesToExecute() {
        when(appointmentRepository.findById(any())).thenReturn(Optional.empty());
        var dto = com.example.consulta.api.dto.appointment.CreateAppointmentDTO.builder()
                .professionalId("prof-1").scheduledAt(LocalDateTime.now().plusDays(1)).build();
        // No professional profile set up → ScheduleAppointmentCommand throws ResourceNotFoundException
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> service.scheduleAppointment("u-1", dto))
                .isInstanceOf(Exception.class);
    }

    @Test
    void cancelAppointment_delegatesToExecute() {
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        var dto = new com.example.consulta.api.dto.appointment.CancelAppointmentDTO();
        dto.setCancellationReason("Motivo");
        service.cancelAppointment("appt-1", "u-1", dto);
        org.assertj.core.api.Assertions.assertThat(appointment.getStatus())
                .isEqualTo(com.example.consulta.domain.enums.AppointmentStatus.CANCELED);
    }

    @Test
    void rateAppointment_delegatesToExecute() {
        appointment.setStatus(com.example.consulta.domain.enums.AppointmentStatus.COMPLETED);
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(patient));
        var dto = new com.example.consulta.api.dto.appointment.RateAppointmentDTO();
        dto.setStars(5); dto.setComment("Ótimo");
        service.rateAppointment("appt-1", "u-1", dto);
        org.assertj.core.api.Assertions.assertThat(appointment.getRating()).isEqualTo(5);
    }
}
