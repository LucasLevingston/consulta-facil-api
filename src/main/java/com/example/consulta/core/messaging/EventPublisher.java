package com.example.consulta.core.messaging;

import com.example.consulta.domain.event.AppointmentCanceledEvent;
import com.example.consulta.domain.event.AppointmentConfirmedEvent;
import com.example.consulta.domain.event.AppointmentCreatedEvent;
import com.example.consulta.domain.event.PaymentFailedEvent;
import com.example.consulta.domain.event.PaymentSucceededEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(@Autowired(required = false) RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishAppointmentCreated(AppointmentCreatedEvent event) {
        publish(RabbitMQConfig.RK_APPOINTMENTS_CREATED, event);
    }

    public void publishAppointmentCanceled(AppointmentCanceledEvent event) {
        publish(RabbitMQConfig.RK_APPOINTMENTS_CANCELED, event);
    }

    public void publishAppointmentConfirmed(AppointmentConfirmedEvent event) {
        publish(RabbitMQConfig.RK_APPOINTMENTS_CONFIRMED, event);
    }

    public void publishPaymentSucceeded(PaymentSucceededEvent event) {
        publish(RabbitMQConfig.RK_PAYMENTS_SUCCEEDED, event);
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        publish(RabbitMQConfig.RK_PAYMENTS_FAILED, event);
    }

    private void publish(String routingKey, Object event) {
        if (rabbitTemplate == null) {
            log.debug("[Messaging] RabbitMQ not configured — event skipped: {}", routingKey);
            return;
        }
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, event);
            log.debug("[Messaging] Published: {}", routingKey);
        } catch (Exception e) {
            log.error("[Messaging] Failed to publish {}: {}", routingKey, e.getMessage());
        }
    }
}
