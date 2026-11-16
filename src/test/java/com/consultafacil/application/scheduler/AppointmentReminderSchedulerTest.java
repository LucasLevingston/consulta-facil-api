package com.consultafacil.application.scheduler;

import com.consultafacil.application.service.whatsapp.WhatsAppService;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.AppointmentModality;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.port.out.EmailPort;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentReminderSchedulerTest {

    @Mock AppointmentRepositoryPort appointmentRepository;
    @Mock EmailPort emailPort;
    @Mock WhatsAppService whatsAppService;

    AppointmentReminderScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new AppointmentReminderScheduler(appointmentRepository, emailPort, whatsAppService);
    }

    private Appointment appointmentFor(User patientUser, int hour) {
        User doctor = User.builder().id("d").name("Dra. Ana").build();
        return Appointment.builder()
                .id("a-" + hour)
                .patient(PatientProfile.builder().id("p").user(patientUser).build())
                .professional(ProfessionalProfile.builder().id("pr").user(doctor).build())
                .scheduledAt(LocalDate.now().plusDays(1).atTime(hour, 30))
                .modality(AppointmentModality.ONLINE)
                .build();
    }

    @Test
    void remindsTomorrowsConfirmedAndPendingAppointmentsByEmailAndWhatsApp() {
        User patient = User.builder().id("u").name("João").email("joao@example.com").phone("+5511900000000").build();
        when(appointmentRepository.findByStatusInAndScheduledAtBetweenOrderByCheckedInAt(any(), any(), any()))
                .thenReturn(List.of(appointmentFor(patient, 9)));

        scheduler.sendDailyReminders();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AppointmentStatus>> statuses = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<LocalDateTime> start = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(appointmentRepository).findByStatusInAndScheduledAtBetweenOrderByCheckedInAt(
                statuses.capture(), start.capture(), any());
        assertThat(statuses.getValue()).containsExactlyInAnyOrder(AppointmentStatus.CONFIRMED, AppointmentStatus.PENDING);
        assertThat(start.getValue().toLocalDate()).isEqualTo(LocalDate.now().plusDays(1));
        verify(emailPort).sendAppointmentReminder("joao@example.com", "João", "Dra. Ana", "09:30", "ONLINE");
        verify(whatsAppService).sendMessage(eq("+5511900000000"), eq(
                "Olá João! ⏰ Lembrete: sua consulta com Dra. Ana é amanhã às 09:30."));
    }

    @Test
    void skipsWhatsAppForPatientsWhoOptedOut() {
        User patient = User.builder().id("u").name("João").email("joao@example.com").phone("+5511900000000")
                .whatsappNotificationsEnabled(false).build();
        when(appointmentRepository.findByStatusInAndScheduledAtBetweenOrderByCheckedInAt(any(), any(), any()))
                .thenReturn(List.of(appointmentFor(patient, 9)));

        scheduler.sendDailyReminders();

        verify(emailPort).sendAppointmentReminder(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(whatsAppService, never()).sendMessage(anyString(), anyString());
    }

    @Test
    void sendsNothingWhenThereAreNoAppointmentsTomorrow() {
        when(appointmentRepository.findByStatusInAndScheduledAtBetweenOrderByCheckedInAt(any(), any(), any()))
                .thenReturn(List.of());

        scheduler.sendDailyReminders();

        org.mockito.Mockito.verifyNoInteractions(emailPort, whatsAppService);
    }

    @Test
    void oneFailingReminderDoesNotStopTheOthers() {
        User first = User.builder().id("u1").name("João").email("joao@example.com").phone("+551190").build();
        User second = User.builder().id("u2").name("Maria").email("maria@example.com").phone("+551191").build();
        when(appointmentRepository.findByStatusInAndScheduledAtBetweenOrderByCheckedInAt(any(), any(), any()))
                .thenReturn(List.of(appointmentFor(first, 9), appointmentFor(second, 10)));
        doThrow(new RuntimeException("SES down")).when(emailPort)
                .sendAppointmentReminder(eq("joao@example.com"), anyString(), anyString(), anyString(), anyString());

        scheduler.sendDailyReminders();

        verify(emailPort, times(2)).sendAppointmentReminder(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(whatsAppService).sendMessage(eq("+551191"), anyString());
    }
}
