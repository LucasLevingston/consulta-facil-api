package com.consultafacil.application.service.appointment;

import com.consultafacil.application.observability.BusinessMetrics;
import com.consultafacil.application.port.in.command.ScheduleAppointmentCommand;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.*;
import com.consultafacil.domain.enums.*;
import com.consultafacil.domain.port.out.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ScheduleAppointmentServiceTest {

    @Mock AppointmentRepositoryPort appointmentRepository;
    @Mock PatientProfileRepositoryPort patientProfileRepository;
    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;
    @Mock ProfessionalServiceRepositoryPort professionalServiceRepository;
    @Mock AppointmentNotificationPort appointmentNotification;
    @Mock AppointmentCheckoutInitiator checkoutInitiator;
    @Mock BusinessMetrics businessMetrics;

    ScheduleAppointmentService service;

    PatientProfile patient;
    ProfessionalProfile professional;

    @BeforeEach
    void setUp() {
        User patientUser = User.builder().id("u-1").email("p@email.com").name("João").password("x").role(UserRole.PATIENT).build();
        patient = new PatientProfile();
        patient.setId("pp-1");
        patient.setUser(patientUser);

        User profUser = User.builder().id("u-2").email("dr@email.com").name("Dra. Ana").password("x").role(UserRole.PROFESSIONAL).build();
        professional = new ProfessionalProfile();
        professional.setId("prof-1");
        professional.setUser(profUser);
        professional.setSpecialty(Specialty.CARDIOLOGIA);

        service = new ScheduleAppointmentService(appointmentRepository, patientProfileRepository,
                professionalProfileRepository, professionalServiceRepository, appointmentNotification,
                checkoutInitiator, businessMetrics, new AppointmentMapper());

        when(appointmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    ScheduleAppointmentCommand scheduleCmd(String serviceId) {
        return new ScheduleAppointmentCommand(
                "u-1", "prof-1", LocalDateTime.now().plusDays(1),
                "Dor de cabeça", null, AppointmentModality.IN_PERSON,
                serviceId, null);
    }

    @Test
    void schedule_serviceRequiresConsultation_throwsBadRequest() {
        com.consultafacil.domain.entity.ProfessionalService consultationService =
                com.consultafacil.domain.entity.ProfessionalService.builder()
                .id("svc-1").name("Limpeza de pele").price(BigDecimal.valueOf(200))
                .durationMinutes(60).requiresConsultation(true).active(true).build();

        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(patient));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));
        when(appointmentRepository.existsByProfessionalIdAndScheduledAt(any(), any())).thenReturn(false);
        when(professionalServiceRepository.findById("svc-1")).thenReturn(Optional.of(consultationService));

        assertThatThrownBy(() -> service.execute(scheduleCmd("svc-1")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("prior consultation");
    }

    @Test
    void schedule_directService_doesNotThrow() {
        com.consultafacil.domain.entity.ProfessionalService directService =
                com.consultafacil.domain.entity.ProfessionalService.builder()
                .id("svc-2").name("Retorno").price(BigDecimal.valueOf(100))
                .durationMinutes(30).requiresConsultation(false).active(true).build();

        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(patient));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));
        when(appointmentRepository.existsByProfessionalIdAndScheduledAt(any(), any())).thenReturn(false);
        when(professionalServiceRepository.findById("svc-2")).thenReturn(Optional.of(directService));
        when(appointmentRepository.save(any())).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId("appt-new");
            return a;
        });

        var result = service.execute(scheduleCmd("svc-2"));

        assertThat(result).isNotNull();
        assertThat(result.getServiceId()).isEqualTo("svc-2");
    }

    @Test
    void schedule_serviceNotFound_throwsNotFound() {
        when(patientProfileRepository.findByUserId("u-1")).thenReturn(Optional.of(patient));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(professional));
        when(appointmentRepository.existsByProfessionalIdAndScheduledAt(any(), any())).thenReturn(false);
        when(professionalServiceRepository.findById("svc-bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(scheduleCmd("svc-bad")))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
