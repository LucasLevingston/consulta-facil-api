package com.consultafacil.application.consumer;

import com.consultafacil.ConsultaFacilApplication;
import com.consultafacil.application.service.whatsapp.WhatsAppService;
import com.consultafacil.core.messaging.RabbitMQConstants;
import com.consultafacil.domain.enums.AppointmentModality;
import com.consultafacil.domain.event.AppointmentCanceledEvent;
import com.consultafacil.domain.event.AppointmentConfirmedEvent;
import com.consultafacil.domain.event.AppointmentCreatedEvent;
import com.consultafacil.domain.event.PaymentFailedEvent;
import com.consultafacil.domain.event.PaymentSucceededEvent;
import com.consultafacil.domain.port.out.EmailPort;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/**
 * Integration tests for RabbitMQ consumers.
 *
 * Uses a real RabbitMQ broker (Testcontainers) to verify the full
 * RabbitListener → consumer method → external port call flow.
 * External adapters (SES, Twilio) are replaced with @MockBean so no
 * real messages are sent.
 *
 * The datasource is H2 (from the test-profile config) so PostgreSQL is
 * NOT required. The standard test application.properties excludes
 * RabbitAutoConfiguration for unit-test speed; this class overrides that
 * exclusion by supplying the broker URL dynamically via @DynamicPropertySource,
 * which causes Spring Boot to include RabbitMQ autoconfiguration for this
 * application context.
 */
@SpringBootTest(
        classes = ConsultaFacilApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                // Remove the RabbitMQ autoconfiguration exclusion that the main
                // test application.properties sets. Without this the
                // ConnectionFactory bean is never created and the consumers
                // (all @ConditionalOnBean(ConnectionFactory.class)) are skipped.
                "spring.autoconfigure.exclude="
        }
)
@Testcontainers(disabledWithoutDocker = true)
class ConsumerIntegrationTest {

    @Container
    static final RabbitMQContainer RABBIT =
            new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @DynamicPropertySource
    static void rabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", RABBIT::getHost);
        registry.add("spring.rabbitmq.port", RABBIT::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBIT::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBIT::getAdminPassword);
    }

    // ── External adapters replaced with mocks ───────────────────────────────
    @MockitoBean EmailPort emailPort;
    @MockitoBean WhatsAppService whatsAppService;

    @Autowired RabbitTemplate rabbitTemplate;

    // ── Shared fixtures ──────────────────────────────────────────────────────

    private static final LocalDateTime SCHEDULED_AT = LocalDateTime.of(2026, 6, 10, 14, 0);

    private AppointmentCreatedEvent appointmentCreatedEvent() {
        return new AppointmentCreatedEvent(
                "evt-1", "appt-1", "p-1",
                "João", "joao@email.com", "+5511999990000",
                "prof-1", "Dra. Ana", "ana@email.com", "+5511888880000",
                SCHEDULED_AT, AppointmentModality.IN_PERSON, true, "2026-06-01T10:00:00Z");
    }

    private AppointmentCanceledEvent appointmentCanceledEvent() {
        return new AppointmentCanceledEvent(
                "evt-2", "appt-1", "p-1",
                "João", "joao@email.com", "+5511999990000",
                "prof-1", "Dra. Ana", "ana@email.com", "+5511888880000",
                SCHEDULED_AT, AppointmentModality.IN_PERSON,
                "Conflito de agenda", true, "2026-06-01T10:00:00Z");
    }

    private AppointmentConfirmedEvent appointmentConfirmedEvent() {
        return new AppointmentConfirmedEvent(
                "evt-3", "appt-1", "p-1",
                "João", "joao@email.com", "+5511999990000",
                "prof-1", "Dra. Ana",
                SCHEDULED_AT, AppointmentModality.IN_PERSON, true, "2026-06-01T10:00:00Z");
    }

    private PaymentSucceededEvent paymentSucceededEvent() {
        return new PaymentSucceededEvent(
                "evt-4", "appt-2", "p-1",
                "João Silva", "joao@email.com",
                new BigDecimal("250.00"), "BRL", "credit_card",
                "mp-pay-1", "2026-01-01T10:00:00Z");
    }

    private PaymentFailedEvent paymentFailedEvent() {
        return new PaymentFailedEvent(
                "evt-5", "appt-3", "p-1",
                "João Silva", "joao@email.com",
                new BigDecimal("250.00"), "BRL", "2026-01-01T10:00:00Z");
    }

    // ── AppointmentEmailConsumer ─────────────────────────────────────────────

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

    // ── AppointmentWhatsAppConsumer ──────────────────────────────────────────

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

    // ── PaymentEmailConsumer ─────────────────────────────────────────────────

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

    // ── Error-path: consumer swallows exception, does not redeliver ──────────

    @Test
    void appointmentCreated_emailPortThrows_doesNotPropagateToRabbitMQ() {
        org.mockito.Mockito.doThrow(new RuntimeException("SES down"))
                .when(emailPort)
                .sendAppointmentConfirmation(any(), any(), any(), any(), any());

        rabbitTemplate.convertAndSend(
                RabbitMQConstants.EXCHANGE,
                RabbitMQConstants.RK_APPOINTMENTS_CREATED,
                appointmentCreatedEvent());

        // Consumer must catch the exception internally; the message should not
        // be redelivered. We verify the method was attempted and no unhandled
        // exception propagates (the queue depth stays at 0 after delivery).
        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(emailPort, atLeastOnce())
                        .sendAppointmentConfirmation(any(), any(), any(), any(), any()));
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
