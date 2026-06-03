package com.example.consulta.application.consumer;

import com.example.consulta.application.service.WhatsAppService;
import com.example.consulta.domain.enums.AppointmentModality;
import com.example.consulta.domain.event.AppointmentCanceledEvent;
import com.example.consulta.domain.event.AppointmentConfirmedEvent;
import com.example.consulta.domain.event.AppointmentCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppointmentWhatsAppConsumerTest {

    @Mock
    WhatsAppService whatsAppService;

    @InjectMocks
    AppointmentWhatsAppConsumer consumer;

    private final LocalDateTime scheduledAt = LocalDateTime.of(2026, 6, 10, 14, 0);

    private AppointmentCreatedEvent createdEvent() {
        return new AppointmentCreatedEvent("evt-1", "appt-1", "p-1",
                "João", "joao@email.com", "+5511999990000",
                "prof-1", "Dra. Ana", "ana@email.com", "+5511888880000",
                scheduledAt, AppointmentModality.IN_PERSON, "2026-06-01T10:00:00Z");
    }

    private AppointmentCanceledEvent canceledEvent() {
        return new AppointmentCanceledEvent("evt-2", "appt-1", "p-1",
                "João", "joao@email.com", "+5511999990000",
                "prof-1", "Dra. Ana", "ana@email.com", "+5511888880000",
                scheduledAt, AppointmentModality.IN_PERSON,
                "Conflito de agenda", "2026-06-01T10:00:00Z");
    }

    private AppointmentConfirmedEvent confirmedEvent() {
        return new AppointmentConfirmedEvent("evt-3", "appt-1", "p-1",
                "João", "joao@email.com", "+5511999990000",
                "prof-1", "Dra. Ana",
                scheduledAt, AppointmentModality.IN_PERSON, "2026-06-01T10:00:00Z");
    }

    @Test
    void onAppointmentCreated_shouldSendWhatsAppToPatient() {
        consumer.onAppointmentCreated(createdEvent());

        verify(whatsAppService).sendMessage(eq("+5511999990000"), contains("João"));
    }

    @Test
    void onAppointmentCreated_messageShouldContainProfessionalName() {
        consumer.onAppointmentCreated(createdEvent());

        verify(whatsAppService).sendMessage(anyString(), contains("Dra. Ana"));
    }

    @Test
    void onAppointmentCanceled_shouldSendToPatientAndProfessional() {
        consumer.onAppointmentCanceled(canceledEvent());

        verify(whatsAppService, times(2)).sendMessage(anyString(), anyString());
        verify(whatsAppService).sendMessage(eq("+5511999990000"), anyString());
        verify(whatsAppService).sendMessage(eq("+5511888880000"), anyString());
    }

    @Test
    void onAppointmentCanceled_patientMessageShouldContainCanceledKeyword() {
        consumer.onAppointmentCanceled(canceledEvent());

        verify(whatsAppService).sendMessage(eq("+5511999990000"), contains("cancelada"));
    }

    @Test
    void onAppointmentConfirmed_shouldSendToPatient() {
        consumer.onAppointmentConfirmed(confirmedEvent());

        verify(whatsAppService).sendMessage(eq("+5511999990000"), contains("confirmada"));
    }
}
