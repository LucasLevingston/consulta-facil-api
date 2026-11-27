package com.consultafacil.application.consumer;

import com.consultafacil.core.messaging.RabbitMQConstants;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AppointmentEmailConsumerIntegrationTest extends ConsumerIntegrationTestBase {

    @Test
    void appointmentCreated_publishedToEmailQueue_sendsConfirmationEmail() {
        rabbitTemplate.convertAndSend(
                RabbitMQConstants.EXCHANGE,
                RabbitMQConstants.RK_APPOINTMENTS_CREATED,
                appointmentCreatedEvent());

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(emailPort).sendAppointmentConfirmation(
                        eq("joao@email.com"),
                        eq("João"),
                        eq("Dra. Ana"),
                        eq("10/06/2026 às 14:00"),
                        eq("IN_PERSON")));
    }

    @Test
    void appointmentCanceled_publishedToEmailQueue_sendsCancellationEmail() {
        rabbitTemplate.convertAndSend(
                RabbitMQConstants.EXCHANGE,
                RabbitMQConstants.RK_APPOINTMENTS_CANCELED,
                appointmentCanceledEvent());

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(emailPort).sendAppointmentCancellation(
                        eq("joao@email.com"),
                        eq("João"),
                        eq("Dra. Ana"),
                        eq("10/06/2026 às 14:00")));
    }

    @Test
    void appointmentConfirmed_publishedToEmailQueue_sendsConfirmedByProfessionalEmail() {
        rabbitTemplate.convertAndSend(
                RabbitMQConstants.EXCHANGE,
                RabbitMQConstants.RK_APPOINTMENTS_CONFIRMED,
                appointmentConfirmedEvent());

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(emailPort).sendAppointmentConfirmedByProfessional(
                        eq("joao@email.com"),
                        eq("João"),
                        eq("Dra. Ana"),
                        eq("10/06/2026 às 14:00")));
    }

    @Test
    void appointmentCreated_emailPortThrows_doesNotPropagateToRabbitMQ() {
        org.mockito.Mockito.doThrow(new RuntimeException("SES down"))
                .when(emailPort)
                .sendAppointmentConfirmation(any(), any(), any(), any(), any());

        rabbitTemplate.convertAndSend(
                RabbitMQConstants.EXCHANGE,
                RabbitMQConstants.RK_APPOINTMENTS_CREATED,
                appointmentCreatedEvent());

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(emailPort, atLeastOnce())
                        .sendAppointmentConfirmation(any(), any(), any(), any(), any()));
    }

    @Test
    void appointmentCreated_emailOptedOut_skipsConfirmationEmail() {
        rabbitTemplate.convertAndSend(
                RabbitMQConstants.EXCHANGE,
                RabbitMQConstants.RK_APPOINTMENTS_CREATED,
                appointmentCreatedEventOptedOut());

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(emailPort, never())
                        .sendAppointmentConfirmation(anyString(), anyString(), anyString(), anyString(), anyString()));
    }

    @Test
    void appointmentCanceled_emailOptedOut_skipsCancellationEmail() {
        rabbitTemplate.convertAndSend(
                RabbitMQConstants.EXCHANGE,
                RabbitMQConstants.RK_APPOINTMENTS_CANCELED,
                appointmentCanceledEventOptedOut());

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(emailPort, never())
                        .sendAppointmentCancellation(anyString(), anyString(), anyString(), anyString()));
    }

    @Test
    void appointmentConfirmed_emailOptedOut_skipsConfirmedByProfessionalEmail() {
        rabbitTemplate.convertAndSend(
                RabbitMQConstants.EXCHANGE,
                RabbitMQConstants.RK_APPOINTMENTS_CONFIRMED,
                appointmentConfirmedEventOptedOut());

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(emailPort, never())
                        .sendAppointmentConfirmedByProfessional(anyString(), anyString(), anyString(), anyString()));
    }
}
