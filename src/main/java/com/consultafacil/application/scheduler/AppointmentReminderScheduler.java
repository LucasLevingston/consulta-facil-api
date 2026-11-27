package com.consultafacil.application.scheduler;

import com.consultafacil.application.service.whatsapp.WhatsAppService;
import com.consultafacil.domain.port.out.EmailPort;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.User;
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
    private final WhatsAppService whatsAppService;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("pt-BR"));

    // Every day at 11:00 BRT (America/Sao_Paulo)
    @Scheduled(cron = "0 0 11 * * *", zone = "America/Sao_Paulo")
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

                User patientUser = appt.getPatient().getUser();
                if (!patientUser.isEmailNotificationsEnabled()) {
                    log.debug("[Reminder] Email opted out for appointment {}", appt.getId());
                } else {
                    emailService.sendAppointmentReminder(patientEmail, patientName, profName, timeStr, modality);
                }
                sendWhatsAppReminder(patientUser, patientName, profName, timeStr);
            } catch (Exception e) {
                log.error("[Reminder] Failed to send reminder for appointment {}: {}", appt.getId(), e.getMessage());
            }
        }
    }

    private void sendWhatsAppReminder(User patient, String patientName, String professionalName, String time) {
        if (!patient.isWhatsappNotificationsEnabled()) return;
        whatsAppService.sendMessage(patient.getPhone(),
                "Olá " + patientName + "! ⏰ Lembrete: sua consulta com " + professionalName
                        + " é amanhã às " + time + ".");
    }
}
