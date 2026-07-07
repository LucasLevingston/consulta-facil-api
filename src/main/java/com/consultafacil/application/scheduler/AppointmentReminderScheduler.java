package com.consultafacil.application.scheduler;

import com.consultafacil.domain.port.out.EmailPort;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentReminderScheduler {

    private final AppointmentRepositoryPort appointmentRepository;
    private final EmailPort emailService;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("pt-BR"));

    // Every day at 08:00 BRT (UTC-3 = 11:00 UTC)
    @Scheduled(cron = "0 0 11 * * *", zone = "UTC")
    public void sendDailyReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDateTime start = tomorrow.atStartOfDay();
        LocalDateTime end = tomorrow.atTime(23, 59, 59);

        List<Appointment> appointments = appointmentRepository
                .findByStatusInAndScheduledAtBetweenOrderByCheckedInAt(
                        List.of(AppointmentStatus.CONFIRMED, AppointmentStatus.PENDING),
                        start, end);

        log.info("[Reminder] Sending reminders for {} appointments on {}", appointments.size(), tomorrow);

        for (Appointment appt : appointments) {
            try {
                String patientEmail = appt.getPatient().getUser().getEmail();
                String patientName  = appt.getPatient().getUser().getName();
                String profName     = appt.getProfessional().getUser().getName();
                String timeStr      = appt.getScheduledAt().format(FMT);
                String modality     = appt.getModality() != null ? appt.getModality().name() : "";

                emailService.sendAppointmentReminder(patientEmail, patientName, profName, timeStr, modality);
            } catch (Exception e) {
                log.error("[Reminder] Failed to send reminder for appointment {}: {}", appt.getId(), e.getMessage());
            }
        }
    }
}
