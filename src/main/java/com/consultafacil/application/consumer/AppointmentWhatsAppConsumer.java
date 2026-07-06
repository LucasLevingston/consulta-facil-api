package com.consultafacil.application.consumer;

import com.consultafacil.application.service.WhatsAppService;
import com.consultafacil.core.messaging.RabbitMQConstants;
import com.consultafacil.domain.event.AppointmentCanceledEvent;
import com.consultafacil.domain.event.AppointmentConfirmedEvent;
import com.consultafacil.domain.event.AppointmentCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(ConnectionFactory.class)
public class AppointmentWhatsAppConsumer {

    private final WhatsAppService whatsAppService;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", Locale.forLanguageTag("pt-BR"));

    @RabbitListener(queues = RabbitMQConstants.Q_APPOINTMENTS_CREATED_WHATSAPP)
    public void onAppointmentCreated(AppointmentCreatedEvent event) {
        log.info("[WhatsApp] Processing appointments.created for appointment {}", event.appointmentId());
        String dateStr = event.scheduledAt().format(FMT);
        whatsAppService.sendMessage(event.patientPhone(),
                "Olá " + event.patientName() + "! ✅ Sua consulta com " + event.professionalName() +
                " foi agendada para " + dateStr + ". Aguardamos você!");
    }

    @RabbitListener(queues = RabbitMQConstants.Q_APPOINTMENTS_CANCELED_WHATSAPP)
    public void onAppointmentCanceled(AppointmentCanceledEvent event) {
        log.info("[WhatsApp] Processing appointments.canceled for appointment {}", event.appointmentId());
        String dateStr = event.scheduledAt().format(FMT);
        whatsAppService.sendMessage(event.patientPhone(),
                "Olá " + event.patientName() + "! ❌ Sua consulta com " + event.professionalName() +
                " em " + dateStr + " foi *cancelada*. Entre em contato para reagendar.");
        whatsAppService.sendMessage(event.professionalPhone(),
                "A consulta com " + event.patientName() + " em " + dateStr + " foi cancelada.");
    }

    @RabbitListener(queues = RabbitMQConstants.Q_APPOINTMENTS_CONFIRMED_WHATSAPP)
    public void onAppointmentConfirmed(AppointmentConfirmedEvent event) {
        log.info("[WhatsApp] Processing appointments.confirmed for appointment {}", event.appointmentId());
        String dateStr = event.scheduledAt().format(FMT);
        whatsAppService.sendMessage(event.patientPhone(),
                "Olá " + event.patientName() + "! 🗓️ Sua consulta com " + event.professionalName() +
                " em " + dateStr + " está *confirmada*. Até lá!");
    }
}
