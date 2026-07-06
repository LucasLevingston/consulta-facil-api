package com.consultafacil.application.consumer;

import com.consultafacil.core.messaging.RabbitMQConstants;
import com.consultafacil.core.util.PiiMask;
import com.consultafacil.domain.event.PaymentFailedEvent;
import com.consultafacil.domain.event.PaymentSucceededEvent;
import com.consultafacil.domain.port.out.EmailPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(ConnectionFactory.class)
public class PaymentEmailConsumer {

    private final EmailPort emailPort;

    @RabbitListener(queues = RabbitMQConstants.Q_PAYMENTS_SUCCEEDED_EMAIL)
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        log.info("[Email] payments.succeeded → to={} appointment={} amount={}",
                PiiMask.maskEmail(event.patientEmail()), event.appointmentId(), event.amount());
        try {
            emailPort.sendPaymentReceipt(
                event.patientEmail(),
                event.patientName(),
                event.appointmentId(),
                event.amount() != null ? event.amount().toPlainString() : "0",
                event.paymentMethod() != null ? event.paymentMethod() : "MercadoPago"
            );
        } catch (Exception e) {
            log.error("[Email] Failed to send payment receipt for appointment {}: {}", event.appointmentId(), e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConstants.Q_PAYMENTS_FAILED_EMAIL)
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.info("[Email] payments.failed → to={} appointment={}",
                PiiMask.maskEmail(event.patientEmail()), event.appointmentId());
        try {
            emailPort.sendPaymentFailure(
                event.patientEmail(),
                event.patientName(),
                event.appointmentId()
            );
        } catch (Exception e) {
            log.error("[Email] Failed to send payment failure notice for appointment {}: {}", event.appointmentId(), e.getMessage());
        }
    }
}
