package com.consultafacil.application.scheduler;

import com.consultafacil.domain.entity.*;
import com.consultafacil.domain.enums.*;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.EmailPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.consultafacil.domain.enums.Specialty;

import java.time.LocalDateTime;
import java.util.List;
import com.consultafacil.domain.enums.Specialty;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import com.consultafacil.domain.enums.Specialty;

@ExtendWith(MockitoExtension.class)
class AppointmentReminderSchedulerTest {

    @Mock AppointmentRepositoryPort appointmentRepository;
    @Mock EmailPort emailService;
    @InjectMocks AppointmentReminderScheduler scheduler;

    Appointment appointment;

    @BeforeEach
    void setUp() {
        User pUser = User.builder().id("u-1").email("p@e.com").name("João").password("x").role(UserRole.PATIENT).build();
        PatientProfile patient = new PatientProfile(); patient.setId("pp-1"); patient.setUser(pUser);
        User dUser = User.builder().id("u-2").email("d@e.com").name("Dra.Ana").password("x").role(UserRole.PROFESSIONAL).build();
        ProfessionalProfile prof = new ProfessionalProfile(); prof.setId("pr-1"); prof.setUser(dUser);
        prof.setSpecialty(Specialty.CARDIOLOGIA);

        appointment = Appointment.schedule(patient, prof, LocalDateTime.now().plusDays(1).withHour(14).withMinute(0),
                "Consulta", null, AppointmentModality.IN_PERSON, null, null, null);
        appointment.setId("appt-1");
    }

    @Test void sendDailyReminders_withAppointments_sendsEmail() {
        when(appointmentRepository.findByStatusInAndScheduledAtBetweenOrderByCheckedInAt(any(), any(), any()))
                .thenReturn(List.of(appointment));

        scheduler.sendDailyReminders();

        verify(emailService).sendAppointmentReminder(
                eq("p@e.com"), eq("João"), eq("Dra.Ana"), anyString(), anyString());
    }

    @Test void sendDailyReminders_noAppointments_noEmailSent() {
        when(appointmentRepository.findByStatusInAndScheduledAtBetweenOrderByCheckedInAt(any(), any(), any()))
                .thenReturn(List.of());

        scheduler.sendDailyReminders();

        verifyNoInteractions(emailService);
    }

    @Test void sendDailyReminders_emailThrows_doesNotPropagateException() {
        when(appointmentRepository.findByStatusInAndScheduledAtBetweenOrderByCheckedInAt(any(), any(), any()))
                .thenReturn(List.of(appointment));
        doThrow(new RuntimeException("SMTP error")).when(emailService)
                .sendAppointmentReminder(any(), any(), any(), any(), any());

        scheduler.sendDailyReminders();

        verify(emailService).sendAppointmentReminder(any(), any(), any(), any(), any());
    }

    @Test void sendDailyReminders_multipleAppointments_sendsForEach() {
        when(appointmentRepository.findByStatusInAndScheduledAtBetweenOrderByCheckedInAt(any(), any(), any()))
                .thenReturn(List.of(appointment, appointment));

        scheduler.sendDailyReminders();

        verify(emailService, times(2)).sendAppointmentReminder(any(), any(), any(), any(), any());
    }
}
