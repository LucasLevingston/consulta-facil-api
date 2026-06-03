package com.example.consulta.core.messaging;

import com.example.consulta.domain.enums.AppointmentModality;
import com.example.consulta.domain.event.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock RabbitTemplate rabbitTemplate;
    @InjectMocks EventPublisher publisher;

    private AppointmentCreatedEvent createdEvent() {
        return new AppointmentCreatedEvent("e-1","a-1","p-1","João","j@e.com","+55",
                "d-1","Dr.","dr@e.com","+55p",LocalDateTime.now(),AppointmentModality.IN_PERSON,"now");
    }

    private PaymentSucceededEvent paymentEvent() {
        return new PaymentSucceededEvent("e-1","a-1","p-1","João","j@e.com",
                new BigDecimal("250"),"BRL","MERCADOPAGO","pay-1","now");
    }

    @Test void publishAppointmentCreated_sendsToCorrectRoutingKey() {
        publisher.publishAppointmentCreated(createdEvent());
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE),
                eq(RabbitMQConfig.RK_APPOINTMENTS_CREATED),
                any(Object.class));
    }

    @Test void publishPaymentSucceeded_sendsToCorrectRoutingKey() {
        publisher.publishPaymentSucceeded(paymentEvent());
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE),
                eq(RabbitMQConfig.RK_PAYMENTS_SUCCEEDED),
                any(Object.class));
    }

    @Test void publishAppointmentCanceled_sendsToCorrectRoutingKey() {
        var event = new AppointmentCanceledEvent("e-1","a-1","p-1","João","j@e.com","+55",
                "d-1","Dr.","dr@e.com","+55p",LocalDateTime.now(),AppointmentModality.IN_PERSON,"r","now");
        publisher.publishAppointmentCanceled(event);
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq(RabbitMQConfig.RK_APPOINTMENTS_CANCELED), any(Object.class));
    }

    @Test void publishAppointmentConfirmed_sendsToCorrectRoutingKey() {
        var event = new AppointmentConfirmedEvent("e-1","a-1","p-1","João","j@e.com","+55",
                "d-1","Dr.",LocalDateTime.now(),AppointmentModality.IN_PERSON,"now");
        publisher.publishAppointmentConfirmed(event);
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq(RabbitMQConfig.RK_APPOINTMENTS_CONFIRMED), any(Object.class));
    }

    @Test void publishPaymentFailed_sendsToCorrectRoutingKey() {
        var event = new PaymentFailedEvent("e-1","a-1","p-1","João","j@e.com",new BigDecimal("250"),"BRL","now");
        publisher.publishPaymentFailed(event);
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq(RabbitMQConfig.RK_PAYMENTS_FAILED), any(Object.class));
    }

    @Test void publish_rabbitTemplateNull_doesNotThrow() {
        EventPublisher publisherNoMq = new EventPublisher(null);
        assertThatCode(() -> publisherNoMq.publishAppointmentCreated(createdEvent())).doesNotThrowAnyException();
    }

    @Test void publish_rabbitTemplateThrows_exceptionCaught() {
        doThrow(new RuntimeException("RabbitMQ down")).when(rabbitTemplate)
                .convertAndSend(anyString(), anyString(), any(Object.class));
        assertThatCode(() -> publisher.publishAppointmentCreated(createdEvent())).doesNotThrowAnyException();
    }
}
