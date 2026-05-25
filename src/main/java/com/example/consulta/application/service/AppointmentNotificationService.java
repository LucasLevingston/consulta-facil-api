package com.example.consulta.application.service;

import com.example.consulta.domain.entity.Appointment;
import com.example.consulta.domain.entity.Notification;
import com.example.consulta.domain.enums.NotificationType;
import com.example.consulta.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentNotificationService {

    private final NotificationRepository notificationRepository;
    private final WhatsAppService whatsAppService;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", Locale.forLanguageTag("pt-BR"));

    public void notifyScheduled(Appointment appointment) {
        String patientName = appointment.getPatient().getUser().getName();
        String professionalName = appointment.getProfessional().getUser().getName();
        String dateStr = appointment.getScheduledAt().format(FMT);

        // In-app → patient
        saveNotification(appointment, NotificationType.APPOINTMENT_SCHEDULED,
                "Consulta agendada",
                "Sua consulta com " + professionalName + " foi agendada para " + dateStr + ".",
                appointment.getPatient().getUser().getId());

        // In-app → professional
        saveNotification(appointment, NotificationType.APPOINTMENT_SCHEDULED,
                "Nova consulta",
                patientName + " agendou uma consulta para " + dateStr + ".",
                appointment.getProfessional().getUser().getId());

        // WhatsApp → patient
        String patientPhone = appointment.getPatient().getUser().getPhone();
        whatsAppService.sendMessage(patientPhone,
                "Olá " + patientName + "! ✅ Sua consulta com " + professionalName +
                " foi agendada para " + dateStr + ". Aguardamos você!");
    }

    public void notifyConfirmed(Appointment appointment) {
        String patientName = appointment.getPatient().getUser().getName();
        String professionalName = appointment.getProfessional().getUser().getName();
        String dateStr = appointment.getScheduledAt().format(FMT);

        // In-app → patient
        saveNotification(appointment, NotificationType.APPOINTMENT_CONFIRMED,
                "Consulta confirmada",
                "Sua consulta com " + professionalName + " em " + dateStr + " foi confirmada.",
                appointment.getPatient().getUser().getId());

        // WhatsApp → patient
        String patientPhone = appointment.getPatient().getUser().getPhone();
        whatsAppService.sendMessage(patientPhone,
                "Olá " + patientName + "! 🗓️ Sua consulta com " + professionalName +
                " em " + dateStr + " está *confirmada*. Até lá!");
    }

    public void notifyCanceled(Appointment appointment) {
        String patientName = appointment.getPatient().getUser().getName();
        String professionalName = appointment.getProfessional().getUser().getName();
        String dateStr = appointment.getScheduledAt().format(FMT);

        // In-app → patient
        saveNotification(appointment, NotificationType.APPOINTMENT_CANCELED,
                "Consulta cancelada",
                "Sua consulta com " + professionalName + " em " + dateStr + " foi cancelada.",
                appointment.getPatient().getUser().getId());

        // WhatsApp → patient
        String patientPhone = appointment.getPatient().getUser().getPhone();
        whatsAppService.sendMessage(patientPhone,
                "Olá " + patientName + "! ❌ Sua consulta com " + professionalName +
                " em " + dateStr + " foi *cancelada*. Entre em contato para reagendar.");
    }

    private void saveNotification(Appointment appointment, NotificationType type,
                                  String title, String message, String targetUserId) {
        try {
            Notification notification = Notification.builder()
                    .type(type)
                    .title(title)
                    .message(message)
                    .targetUser(appointment.getPatient().getUser().getId().equals(targetUserId)
                            ? appointment.getPatient().getUser()
                            : appointment.getProfessional().getUser())
                    .build();
            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("[Notification] Falha ao salvar notificação in-app: {}", e.getMessage());
        }
    }
}
