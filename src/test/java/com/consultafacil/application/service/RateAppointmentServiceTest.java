package com.consultafacil.application.service;

import com.consultafacil.application.port.in.command.RateAppointmentCommand;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.*;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RateAppointmentServiceTest {

    @Mock AppointmentRepositoryPort appointmentRepository;
    @Mock PatientProfileRepositoryPort patientProfileRepository;

    RateAppointmentService service;
    PatientProfile patient;
    Appointment appointment;

    @BeforeEach
    void setUp() {
        User patientUser = User.builder().id("u-1").email("p@email.com").name("João").password("x").role(UserRole.PATIENT).build();
        patient = new PatientProfile();
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
        appointment.setScheduledAt(LocalDateTime.now().plusDays(5));

        service = new RateAppointmentService(appointmentRepository, patientProfileRepository, new AppointmentMapper());
        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void rate_validCompletedAppointment_setsRating() {
        appointment.setStatus(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findById("appt-1")).thenReturn(Optional.of(appointment));
        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(patient));

        service.execute(new RateAppointmentCommand("appt-1", "u-1", 5, "Ótimo!"));

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
}
