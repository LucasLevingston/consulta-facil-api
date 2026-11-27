package com.consultafacil.application.consumer;

import com.consultafacil.ConsultaFacilApplication;
import com.consultafacil.application.service.whatsapp.WhatsAppService;
import com.consultafacil.domain.enums.AppointmentModality;
import com.consultafacil.domain.event.AppointmentCanceledEvent;
import com.consultafacil.domain.event.AppointmentConfirmedEvent;
import com.consultafacil.domain.event.AppointmentCreatedEvent;
import com.consultafacil.domain.event.PaymentFailedEvent;
import com.consultafacil.domain.event.PaymentSucceededEvent;
import com.consultafacil.domain.port.out.EmailPort;
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

@SpringBootTest(
        classes = ConsultaFacilApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = { "spring.autoconfigure.exclude=" }
)
@Testcontainers(disabledWithoutDocker = true)
abstract class ConsumerIntegrationTestBase {

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

    @MockitoBean EmailPort emailPort;
    @MockitoBean WhatsAppService whatsAppService;

    @Autowired RabbitTemplate rabbitTemplate;

    static final LocalDateTime SCHEDULED_AT = LocalDateTime.of(2026, 6, 10, 14, 0);

    AppointmentCreatedEvent appointmentCreatedEvent() {
        return new AppointmentCreatedEvent(
                "evt-1", "appt-1", "p-1",
                "João", "joao@email.com", "+5511999990000",
                "prof-1", "Dra. Ana", "ana@email.com", "+5511888880000",
                SCHEDULED_AT, AppointmentModality.IN_PERSON, true, "2026-06-01T10:00:00Z");
    }

    AppointmentCreatedEvent appointmentCreatedEventOptedOut() {
        return new AppointmentCreatedEvent(
                "evt-1b", "appt-1b", "p-1",
                "João", "joao@email.com", "+5511999990000",
                "prof-1", "Dra. Ana", "ana@email.com", "+5511888880000",
                SCHEDULED_AT, AppointmentModality.IN_PERSON, false, "2026-06-01T10:00:00Z");
    }

    AppointmentCanceledEvent appointmentCanceledEvent() {
        return new AppointmentCanceledEvent(
                "evt-2", "appt-1", "p-1",
                "João", "joao@email.com", "+5511999990000",
                "prof-1", "Dra. Ana", "ana@email.com", "+5511888880000",
                SCHEDULED_AT, AppointmentModality.IN_PERSON,
                "Conflito de agenda", true, "2026-06-01T10:00:00Z");
    }

    AppointmentCanceledEvent appointmentCanceledEventOptedOut() {
        return new AppointmentCanceledEvent(
                "evt-2b", "appt-1b", "p-1",
                "João", "joao@email.com", "+5511999990000",
                "prof-1", "Dra. Ana", "ana@email.com", "+5511888880000",
                SCHEDULED_AT, AppointmentModality.IN_PERSON,
                "Conflito de agenda", false, "2026-06-01T10:00:00Z");
    }

    AppointmentConfirmedEvent appointmentConfirmedEvent() {
        return new AppointmentConfirmedEvent(
                "evt-3", "appt-1", "p-1",
                "João", "joao@email.com", "+5511999990000",
                "prof-1", "Dra. Ana",
                SCHEDULED_AT, AppointmentModality.IN_PERSON, true, "2026-06-01T10:00:00Z");
    }

    AppointmentConfirmedEvent appointmentConfirmedEventOptedOut() {
        return new AppointmentConfirmedEvent(
                "evt-3b", "appt-1b", "p-1",
                "João", "joao@email.com", "+5511999990000",
                "prof-1", "Dra. Ana",
                SCHEDULED_AT, AppointmentModality.IN_PERSON, false, "2026-06-01T10:00:00Z");
    }

    PaymentSucceededEvent paymentSucceededEvent() {
        return new PaymentSucceededEvent(
                "evt-4", "appt-2", "p-1",
                "João Silva", "joao@email.com",
                new BigDecimal("250.00"), "BRL", "credit_card",
                "mp-pay-1", "2026-01-01T10:00:00Z");
    }

    PaymentFailedEvent paymentFailedEvent() {
        return new PaymentFailedEvent(
                "evt-5", "appt-3", "p-1",
                "João Silva", "joao@email.com",
                new BigDecimal("250.00"), "BRL", "2026-01-01T10:00:00Z");
    }
}
