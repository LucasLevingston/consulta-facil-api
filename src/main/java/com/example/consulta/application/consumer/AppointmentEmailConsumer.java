package com.example.consulta.application.consumer;

import com.example.consulta.core.messaging.RabbitMQConfig;
import com.example.consulta.core.util.PiiMask;
import com.example.consulta.domain.event.AppointmentCanceledEvent;
import com.example.consulta.domain.event.AppointmentConfirmedEvent;
import com.example.consulta.domain.event.AppointmentCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnBean(ConnectionFactory.class)
public class AppointmentEmailConsumer {

    @RabbitListener(queues = RabbitMQConfig.Q_APPOINTMENTS_CREATED_EMAIL)
    public void onAppointmentCreated(AppointmentCreatedEvent event) {
        log.info("[Email] appointments.created → to={} appointment={}", PiiMask.maskEmail(event.patientEmail()), event.appointmentId());
        // TODO(spec-005): send via AWS SES — EmailService.sendAppointmentConfirmation(event)
    }

    @RabbitListener(queues = RabbitMQConfig.Q_APPOINTMENTS_CANCELED_EMAIL)
    public void onAppointmentCanceled(AppointmentCanceledEvent event) {
        log.info("[Email] appointments.canceled → to={} appointment={}", PiiMask.maskEmail(event.patientEmail()), event.appointmentId());
        // TODO(spec-005): send via AWS SES — EmailService.sendAppointmentCancellation(event)
    }

    @RabbitListener(queues = RabbitMQConfig.Q_APPOINTMENTS_CONFIRMED_EMAIL)
    public void onAppointmentConfirmed(AppointmentConfirmedEvent event) {
        log.info("[Email] appointments.confirmed → to={} appointment={}", PiiMask.maskEmail(event.patientEmail()), event.appointmentId());
        // TODO(spec-005): send via AWS SES — EmailService.sendAppointmentConfirmation(event)
    }
}
