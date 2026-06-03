package com.example.consulta.application.consumer;

import com.example.consulta.domain.enums.AppointmentModality;
import com.example.consulta.domain.event.AppointmentCanceledEvent;
import com.example.consulta.domain.event.AppointmentConfirmedEvent;
import com.example.consulta.domain.event.AppointmentCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class AppointmentEmailConsumerTest {

    @InjectMocks
    AppointmentEmailConsumer consumer;

    private AppointmentCreatedEvent createdEvent() {
        return new AppointmentCreatedEvent("evt-1", "appt-1", "p-1",
                "João", "joao@email.com", "+5511999990000",
                "prof-1", "Dra. Ana", "ana@email.com", "+5511888880000",
                LocalDateTime.of(2026, 6, 10, 14, 0), AppointmentModality.IN_PERSON, "2026-06-01T10:00:00Z");
    }

    private AppointmentCanceledEvent canceledEvent() {
        return new AppointmentCanceledEvent("evt-2", "appt-1", "p-1",
                "João", "joao@email.com", "+5511999990000",
                "prof-1", "Dra. Ana", "ana@email.com", "+5511888880000",
                LocalDateTime.of(2026, 6, 10, 14, 0), AppointmentModality.IN_PERSON,
                "Conflito de agenda", "2026-06-01T10:00:00Z");
    }

    private AppointmentConfirmedEvent confirmedEvent() {
        return new AppointmentConfirmedEvent("evt-3", "appt-1", "p-1",
                "João", "joao@email.com", "+5511999990000",
                "prof-1", "Dra. Ana",
                LocalDateTime.of(2026, 6, 10, 14, 0), AppointmentModality.IN_PERSON, "2026-06-01T10:00:00Z");
    }

    @Test
    void onAppointmentCreated_shouldNotThrow() {
        consumer.onAppointmentCreated(createdEvent());
    }

    @Test
    void onAppointmentCanceled_shouldNotThrow() {
        consumer.onAppointmentCanceled(canceledEvent());
    }

    @Test
    void onAppointmentConfirmed_shouldNotThrow() {
        consumer.onAppointmentConfirmed(confirmedEvent());
    }
}
