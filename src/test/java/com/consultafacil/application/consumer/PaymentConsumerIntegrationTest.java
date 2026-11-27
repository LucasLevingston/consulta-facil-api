package com.consultafacil.application.consumer;

import com.consultafacil.core.messaging.RabbitMQConstants;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

class PaymentConsumerIntegrationTest extends ConsumerIntegrationTestBase {

    @Test
    void paymentSucceeded_publishedToEmailQueue_sendsReceiptEmail() {
        rabbitTemplate.convertAndSend(
                RabbitMQConstants.EXCHANGE,
                RabbitMQConstants.RK_PAYMENTS_SUCCEEDED,
                paymentSucceededEvent());

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(emailPort).sendPaymentReceipt(
                        eq("joao@email.com"),
                        eq("João Silva"),
                        eq("appt-2"),
                        eq("250.00"),
                        eq("credit_card")));
    }

    @Test
    void paymentFailed_publishedToEmailQueue_sendsFailureEmail() {
        rabbitTemplate.convertAndSend(
                RabbitMQConstants.EXCHANGE,
                RabbitMQConstants.RK_PAYMENTS_FAILED,
                paymentFailedEvent());

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(emailPort).sendPaymentFailure(
                        eq("joao@email.com"),
                        eq("João Silva"),
                        eq("appt-3")));
    }

    @Test
    void paymentSucceeded_emailPortThrows_doesNotPropagateToRabbitMQ() {
        org.mockito.Mockito.doThrow(new RuntimeException("SMTP error"))
                .when(emailPort)
                .sendPaymentReceipt(any(), any(), any(), any(), any());

        rabbitTemplate.convertAndSend(
                RabbitMQConstants.EXCHANGE,
                RabbitMQConstants.RK_PAYMENTS_SUCCEEDED,
                paymentSucceededEvent());

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(emailPort, atLeastOnce())
                        .sendPaymentReceipt(any(), any(), any(), any(), any()));
    }
}
