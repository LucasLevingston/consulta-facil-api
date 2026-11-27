package com.consultafacil.application.consumer;

import com.consultafacil.core.messaging.RabbitMQConstants;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

class AppointmentWhatsAppConsumerIntegrationTest extends ConsumerIntegrationTestBase {

    @Test
    void appointmentCreated_publishedToWhatsAppQueue_sendsWhatsAppToPatient() {
        rabbitTemplate.convertAndSend(
                RabbitMQConstants.EXCHANGE,
                RabbitMQConstants.RK_APPOINTMENTS_CREATED,
                appointmentCreatedEvent());

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(whatsAppService, atLeastOnce()).sendMessage(
                        eq("+5511999990000"),
                        contains("João")));
    }

    @Test
    void appointmentCanceled_publishedToWhatsAppQueue_sendsWhatsAppToBothParties() {
        rabbitTemplate.convertAndSend(
                RabbitMQConstants.EXCHANGE,
                RabbitMQConstants.RK_APPOINTMENTS_CANCELED,
                appointmentCanceledEvent());

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(whatsAppService, atLeastOnce()).sendMessage(
                    eq("+5511999990000"), anyString());
            verify(whatsAppService, atLeastOnce()).sendMessage(
                    eq("+5511888880000"), anyString());
        });
    }

    @Test
    void appointmentConfirmed_publishedToWhatsAppQueue_sendsWhatsAppToPatient() {
        rabbitTemplate.convertAndSend(
                RabbitMQConstants.EXCHANGE,
                RabbitMQConstants.RK_APPOINTMENTS_CONFIRMED,
                appointmentConfirmedEvent());

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(whatsAppService, atLeastOnce()).sendMessage(
                        eq("+5511999990000"),
                        contains("confirmada")));
    }
}
