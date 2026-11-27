package com.consultafacil.application.consumer;

import com.consultafacil.domain.enums.AppointmentModality;
import com.consultafacil.domain.event.AppointmentCanceledEvent;
import com.consultafacil.domain.event.AppointmentConfirmedEvent;
import com.consultafacil.domain.event.AppointmentCreatedEvent;
import com.consultafacil.domain.port.out.EmailPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentEmailConsumerTest {

    @Mock
    EmailPort emailPort;

    @InjectMocks
    AppointmentEmailConsumer consumer;

    private AppointmentCreatedEvent createdEvent() {
        return new AppointmentCreatedEvent("evt-1", "appt-1", "p-1",
                "João", "joao@email.com", "+5511999990000",
                "prof-1", "Dra. Ana", "ana@email.com", "+5511888880000",
                LocalDateTime.of(2026, 6, 10, 14, 0), AppointmentModality.IN_PERSON, true, "2026-06-01T10:00:00Z");
    }

    private AppointmentCanceledEvent canceledEvent() {
        return new AppointmentCanceledEvent("evt-2", "appt-1", "p-1",
                "João", "joao@email.com", "+5511999990000",
                "prof-1", "Dra. Ana", "ana@email.com", "+5511888880000",
                LocalDateTime.of(2026, 6, 10, 14, 0), AppointmentModality.IN_PERSON,
                "Conflito de agenda", true, "2026-06-01T10:00:00Z");
    }

    private AppointmentConfirmedEvent confirmedEvent() {
        return new AppointmentConfirmedEvent("evt-3", "appt-1", "p-1",
                "João", "joao@email.com", "+5511999990000",
                "prof-1", "Dra. Ana",
                LocalDateTime.of(2026, 6, 10, 14, 0), AppointmentModality.IN_PERSON, true, "2026-06-01T10:00:00Z");
    }

    @Test
    void onAppointmentCreated_shouldSendConfirmation() {
        consumer.onAppointmentCreated(createdEvent());

        verify(emailPort).sendAppointmentConfirmation(
                "joao@email.com", "João", "Dra. Ana", "10/06/2026 às 14:00", "IN_PERSON");
    }

    @Test
    void onAppointmentCreated_emailPortThrows_shouldNotPropagateException() {
        doThrow(new RuntimeException("SES error")).when(emailPort)
                .sendAppointmentConfirmation(any(), any(), any(), any(), any());

        consumer.onAppointmentCreated(createdEvent());

        verify(emailPort).sendAppointmentConfirmation(any(), any(), any(), any(), any());
    }

    @Test
    void onAppointmentCanceled_shouldSendCancellation() {
        consumer.onAppointmentCanceled(canceledEvent());

        verify(emailPort).sendAppointmentCancellation(
                "joao@email.com", "João", "Dra. Ana", "10/06/2026 às 14:00");
    }

    @Test
    void onAppointmentCanceled_emailPortThrows_shouldNotPropagateException() {
        doThrow(new RuntimeException("SES error")).when(emailPort)
                .sendAppointmentCancellation(any(), any(), any(), any());

        consumer.onAppointmentCanceled(canceledEvent());

        verify(emailPort).sendAppointmentCancellation(any(), any(), any(), any());
    }

    @Test
    void onAppointmentConfirmed_shouldSendConfirmedByProfessional() {
        consumer.onAppointmentConfirmed(confirmedEvent());

        verify(emailPort).sendAppointmentConfirmedByProfessional(
                "joao@email.com", "João", "Dra. Ana", "10/06/2026 às 14:00");
    }

    @Test
    void onAppointmentConfirmed_emailPortThrows_shouldNotPropagateException() {
        doThrow(new RuntimeException("SES error")).when(emailPort)
                .sendAppointmentConfirmedByProfessional(any(), any(), any(), any());

        consumer.onAppointmentConfirmed(confirmedEvent());

        verify(emailPort).sendAppointmentConfirmedByProfessional(any(), any(), any(), any());
    }
}
