package com.consultafacil.application.service;

import com.consultafacil.core.config.MercadoPagoConfig;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.*;
import com.consultafacil.domain.enums.AppointmentPaymentStatus;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateAppointmentPaymentServiceTest {

    @Mock AppointmentRepositoryPort appointmentRepository;
    @Mock PatientProfileRepositoryPort patientProfileRepository;
    @Mock MercadoPagoConfig mpConfig;

    @InjectMocks CreateAppointmentPaymentService service;

    PatientProfile patient;
    ProfessionalProfile professional;
    Appointment appointment;

    @BeforeEach
    void setUp() {
        User pUser = User.builder().id("u-1").email("p@email.com").name("João").password("x").role(UserRole.PATIENT).build();
        patient = new PatientProfile();
        patient.setId("pp-1");
        patient.setUser(pUser);

        User dUser = User.builder().id("u-2").email("dr@email.com").name("Dra. Ana").password("x").role(UserRole.PROFESSIONAL).build();
        professional = new ProfessionalProfile();
        professional.setId("prof-1");
        professional.setUser(dUser);
        professional.setSpecialty("Cardiologia");
        professional.setConsultationPrice(new BigDecimal("250.00"));
        professional.setAcceptedPaymentMethods(new HashSet<>());

        appointment = new Appointment();
        appointment.setId("appt-1");
        appointment.setPatient(patient);
        appointment.setProfessional(professional);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setPaymentStatus(AppointmentPaymentStatus.UNPAID);
    }

    @Test
    void execute_patientNotFound_throwsNotFound() {
        when(patientProfileRepository.findByUserId("bad")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.execute("appt-1", "bad", null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_appointmentNotFound_throwsNotFound() {
        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(patient));
        when(appointmentRepository.findById("bad")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.execute("bad", "u-1", null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_wrongPatient_throwsBadRequest() {
        PatientProfile other = new PatientProfile();
        other.setId("pp-other");
        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(other));
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        assertThatThrownBy(() -> service.execute("appt-1", "u-1", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("belong to this patient");
    }

    @Test
    void execute_canceledAppointment_throwsBadRequest() {
        appointment.setStatus(AppointmentStatus.CANCELED);
        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(patient));
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        assertThatThrownBy(() -> service.execute("appt-1", "u-1", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("canceled or completed");
    }

    @Test
    void execute_completedAppointment_throwsBadRequest() {
        appointment.setStatus(AppointmentStatus.COMPLETED);
        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(patient));
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        assertThatThrownBy(() -> service.execute("appt-1", "u-1", null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void execute_alreadyPaid_throwsBadRequest() {
        appointment.setPaymentStatus(AppointmentPaymentStatus.PAID);
        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(patient));
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        assertThatThrownBy(() -> service.execute("appt-1", "u-1", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already paid");
    }
}
