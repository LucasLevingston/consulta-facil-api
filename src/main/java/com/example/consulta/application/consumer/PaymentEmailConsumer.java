package com.example.consulta.application.consumer;

import com.example.consulta.core.messaging.RabbitMQConfig;
import com.example.consulta.domain.event.PaymentFailedEvent;
import com.example.consulta.domain.event.PaymentSucceededEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnBean(ConnectionFactory.class)
public class PaymentEmailConsumer {

    @RabbitListener(queues = RabbitMQConfig.Q_PAYMENTS_SUCCEEDED_EMAIL)
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        log.info("[Email] payments.succeeded → to={} appointment={} amount={}",
                event.patientEmail(), event.appointmentId(), event.amount());
        // TODO(spec-005): send payment receipt via AWS SES
    }

    @RabbitListener(queues = RabbitMQConfig.Q_PAYMENTS_FAILED_EMAIL)
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.info("[Email] payments.failed → to={} appointment={}",
                event.patientEmail(), event.appointmentId());
        // TODO(spec-005): send payment failure notice via AWS SES
    }
}
