package com.example.consulta.application.consumer;

import com.example.consulta.domain.event.PaymentFailedEvent;
import com.example.consulta.domain.event.PaymentSucceededEvent;
import com.example.consulta.domain.port.out.EmailPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEmailConsumerTest {

    @Mock
    EmailPort emailPort;

    @InjectMocks
    PaymentEmailConsumer consumer;

    private PaymentSucceededEvent succeededEvent() {
        return new PaymentSucceededEvent("evt-1", "appt-1", "patient-1",
                "João Silva", "joao@email.com", new BigDecimal("250.00"),
                "BRL", "credit_card", "mp-pay-1", "2026-01-01T10:00:00Z");
    }

    private PaymentFailedEvent failedEvent() {
        return new PaymentFailedEvent("evt-2", "appt-2", "patient-1",
                "João Silva", "joao@email.com", new BigDecimal("250.00"),
                "BRL", "2026-01-01T10:00:00Z");
    }

    @Test
    void onPaymentSucceeded_shouldSendReceipt() {
        consumer.onPaymentSucceeded(succeededEvent());

        verify(emailPort).sendPaymentReceipt(
                "joao@email.com", "João Silva", "appt-1", "250.00", "credit_card");
    }

    @Test
    void onPaymentSucceeded_nullAmount_shouldSendWithZero() {
        var event = new PaymentSucceededEvent("evt-1", "appt-1", "patient-1",
                "João Silva", "joao@email.com", null,
                "BRL", "credit_card", "mp-pay-1", "2026-01-01T10:00:00Z");

        consumer.onPaymentSucceeded(event);

        verify(emailPort).sendPaymentReceipt("joao@email.com", "João Silva", "appt-1", "0", "credit_card");
    }

    @Test
    void onPaymentSucceeded_nullPaymentMethod_shouldUseFallback() {
        var event = new PaymentSucceededEvent("evt-1", "appt-1", "patient-1",
                "João Silva", "joao@email.com", new BigDecimal("100.00"),
                "BRL", null, "mp-pay-1", "2026-01-01T10:00:00Z");

        consumer.onPaymentSucceeded(event);

        verify(emailPort).sendPaymentReceipt("joao@email.com", "João Silva", "appt-1", "100.00", "MercadoPago");
    }

    @Test
    void onPaymentSucceeded_emailPortThrows_shouldNotPropagateException() {
        doThrow(new RuntimeException("SMTP error")).when(emailPort)
                .sendPaymentReceipt(any(), any(), any(), any(), any());

        consumer.onPaymentSucceeded(succeededEvent());

        verify(emailPort).sendPaymentReceipt(any(), any(), any(), any(), any());
    }

    @Test
    void onPaymentFailed_shouldSendFailureNotice() {
        consumer.onPaymentFailed(failedEvent());

        verify(emailPort).sendPaymentFailure("joao@email.com", "João Silva", "appt-2");
    }

    @Test
    void onPaymentFailed_emailPortThrows_shouldNotPropagateException() {
        doThrow(new RuntimeException("SMTP error")).when(emailPort)
                .sendPaymentFailure(any(), any(), any());

        consumer.onPaymentFailed(failedEvent());

        verify(emailPort).sendPaymentFailure(any(), any(), any());
    }
}
