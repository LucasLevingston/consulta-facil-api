package com.example.consulta.application.service;

import com.example.consulta.core.messaging.EventPublisher;
import com.example.consulta.core.messaging.event.AppointmentCanceledEvent;
import com.example.consulta.core.messaging.event.AppointmentConfirmedEvent;
import com.example.consulta.core.messaging.event.AppointmentCreatedEvent;
import com.example.consulta.domain.entity.Appointment;
import com.example.consulta.domain.entity.Notification;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.NotificationType;
import com.example.consulta.domain.port.out.AppointmentNotificationPort;
import com.example.consulta.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentNotificationService implements AppointmentNotificationPort {

    private final NotificationRepository notificationRepository;
    private final EventPublisher eventPublisher;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", Locale.forLanguageTag("pt-BR"));

    public void notifyScheduled(Appointment appointment) {
        String patientName = appointment.getPatient().getUser().getName();
        String professionalName = appointment.getProfessional().getUser().getName();
        String dateStr = appointment.getScheduledAt().format(FMT);

        saveNotification(appointment.getPatient().getUser(), NotificationType.APPOINTMENT_SCHEDULED,
                "Consulta agendada",
                "Sua consulta com " + professionalName + " foi agendada para " + dateStr + ".");

        saveNotification(appointment.getProfessional().getUser(), NotificationType.APPOINTMENT_SCHEDULED,
                "Nova consulta",
                patientName + " agendou uma consulta para " + dateStr + ".");

        eventPublisher.publishAppointmentCreated(new AppointmentCreatedEvent(
                UUID.randomUUID().toString(),
                appointment.getId(),
                appointment.getPatient().getId(),
                patientName,
                appointment.getPatient().getUser().getEmail(),
                appointment.getPatient().getUser().getPhone(),
                appointment.getProfessional().getId(),
                professionalName,
                appointment.getProfessional().getUser().getEmail(),
                appointment.getProfessional().getUser().getPhone(),
                appointment.getScheduledAt(),
                appointment.getModality(),
                LocalDateTime.now().toString()
        ));
    }

    public void notifyConfirmed(Appointment appointment) {
        String professionalName = appointment.getProfessional().getUser().getName();
        String dateStr = appointment.getScheduledAt().format(FMT);

        saveNotification(appointment.getPatient().getUser(), NotificationType.APPOINTMENT_CONFIRMED,
                "Consulta confirmada",
                "Sua consulta com " + professionalName + " em " + dateStr + " foi confirmada.");

        eventPublisher.publishAppointmentConfirmed(new AppointmentConfirmedEvent(
                UUID.randomUUID().toString(),
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getPatient().getUser().getName(),
                appointment.getPatient().getUser().getEmail(),
                appointment.getPatient().getUser().getPhone(),
                appointment.getProfessional().getId(),
                professionalName,
                appointment.getScheduledAt(),
                appointment.getModality(),
                LocalDateTime.now().toString()
        ));
    }

    public void notifyCanceled(Appointment appointment) {
        String patientName = appointment.getPatient().getUser().getName();
        String professionalName = appointment.getProfessional().getUser().getName();
        String dateStr = appointment.getScheduledAt().format(FMT);

        saveNotification(appointment.getPatient().getUser(), NotificationType.APPOINTMENT_CANCELED,
                "Consulta cancelada",
                "Sua consulta com " + professionalName + " em " + dateStr + " foi cancelada.");

        saveNotification(appointment.getProfessional().getUser(), NotificationType.APPOINTMENT_CANCELED,
                "Consulta cancelada",
                "A consulta com " + patientName + " em " + dateStr + " foi cancelada.");

        eventPublisher.publishAppointmentCanceled(new AppointmentCanceledEvent(
                UUID.randomUUID().toString(),
                appointment.getId(),
                appointment.getPatient().getId(),
                patientName,
                appointment.getPatient().getUser().getEmail(),
                appointment.getPatient().getUser().getPhone(),
                appointment.getProfessional().getId(),
                professionalName,
                appointment.getProfessional().getUser().getEmail(),
                appointment.getProfessional().getUser().getPhone(),
                appointment.getScheduledAt(),
                appointment.getModality(),
                appointment.getCancellationReason(),
                LocalDateTime.now().toString()
        ));
    }

    private void saveNotification(User user, NotificationType type, String title, String message) {
        try {
            notificationRepository.save(Notification.builder()
                    .type(type)
                    .title(title)
                    .message(message)
                    .targetUser(user)
                    .build());
        } catch (Exception e) {
            log.error("[Notification] Failed to save in-app notification: {}", e.getMessage());
        }
    }
}
