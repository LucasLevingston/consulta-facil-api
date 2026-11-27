package com.consultafacil.application.consumer;

import com.consultafacil.core.messaging.RabbitMQConstants;
import com.consultafacil.core.util.PiiMask;
import com.consultafacil.domain.event.AppointmentCanceledEvent;
import com.consultafacil.domain.event.AppointmentConfirmedEvent;
import com.consultafacil.domain.event.AppointmentCreatedEvent;
import com.consultafacil.domain.port.out.EmailPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(ConnectionFactory.class)
public class AppointmentEmailConsumer {

    private final EmailPort emailPort;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", Locale.forLanguageTag("pt-BR"));

    @RabbitListener(queues = RabbitMQConstants.Q_APPOINTMENTS_CREATED_EMAIL)
    public void onAppointmentCreated(AppointmentCreatedEvent event) {
        if (!event.emailNotificationsEnabled()) {
            log.info("[Email] Patient opted out of email notifications, skipping appointment={}", event.appointmentId());
            return;
        }
        log.info("[Email] appointments.created → to={} appointment={}", PiiMask.maskEmail(event.patientEmail()), event.appointmentId());
        try {
            emailPort.sendAppointmentConfirmation(
                    event.patientEmail(),
                    event.patientName(),
                    event.professionalName(),
                    event.scheduledAt().format(FMT),
                    event.modality().name()
            );
        } catch (Exception e) {
            log.error("[Email] Failed to send appointment confirmation for appointment {}: {}", event.appointmentId(), e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConstants.Q_APPOINTMENTS_CANCELED_EMAIL)
    public void onAppointmentCanceled(AppointmentCanceledEvent event) {
        if (!event.emailNotificationsEnabled()) {
            log.info("[Email] Patient opted out of email notifications, skipping appointment={}", event.appointmentId());
            return;
        }
        log.info("[Email] appointments.canceled → to={} appointment={}", PiiMask.maskEmail(event.patientEmail()), event.appointmentId());
        try {
            emailPort.sendAppointmentCancellation(
                    event.patientEmail(),
                    event.patientName(),
                    event.professionalName(),
                    event.scheduledAt().format(FMT)
            );
        } catch (Exception e) {
            log.error("[Email] Failed to send appointment cancellation for appointment {}: {}", event.appointmentId(), e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConstants.Q_APPOINTMENTS_CONFIRMED_EMAIL)
    public void onAppointmentConfirmed(AppointmentConfirmedEvent event) {
        if (!event.emailNotificationsEnabled()) {
            log.info("[Email] Patient opted out of email notifications, skipping appointment={}", event.appointmentId());
            return;
        }
        log.info("[Email] appointments.confirmed → to={} appointment={}", PiiMask.maskEmail(event.patientEmail()), event.appointmentId());
        try {
            emailPort.sendAppointmentConfirmedByProfessional(
                    event.patientEmail(),
                    event.patientName(),
                    event.professionalName(),
                    event.scheduledAt().format(FMT)
            );
        } catch (Exception e) {
            log.error("[Email] Failed to send appointment confirmed notice for appointment {}: {}", event.appointmentId(), e.getMessage());
        }
    }
}
