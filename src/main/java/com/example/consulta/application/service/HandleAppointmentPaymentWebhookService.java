package com.example.consulta.application.service;

import com.example.consulta.application.observability.BusinessMetrics;
import com.example.consulta.core.messaging.EventPublisher;
import com.example.consulta.domain.entity.Appointment;
import com.example.consulta.domain.enums.AppointmentPaymentStatus;
import com.example.consulta.domain.event.PaymentFailedEvent;
import com.example.consulta.domain.event.PaymentSucceededEvent;
import com.example.consulta.domain.port.out.AppointmentRepositoryPort;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import com.example.consulta.application.port.in.HandlePaymentWebhookUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HandleAppointmentPaymentWebhookService implements HandlePaymentWebhookUseCase {

    private final AppointmentRepositoryPort appointmentRepository;
    private final BusinessMetrics businessMetrics;
    private final EventPublisher eventPublisher;

    @Transactional
    public void execute(Map<String, Object> body) {
        try {
            String type = String.valueOf(body.getOrDefault("type", ""));
            if (!"payment".equals(type)) return;

            Object dataObj = body.get("data");
            if (!(dataObj instanceof Map<?, ?> data)) return;

            Object idObj = data.get("id");
            String paymentId = idObj != null ? String.valueOf(idObj) : "";
            if (paymentId.isBlank()) return;

            Payment payment = new PaymentClient().get(Long.parseLong(paymentId));

            String externalRef = payment.getExternalReference();
            if (externalRef == null || !externalRef.startsWith("appointment|")) return;

            String appointmentId = externalRef.split("\\|")[1];

            if (!"approved".equals(payment.getStatus())) {
                log.info("Pagamento {} status {} para consulta {} — sem mudança", paymentId, payment.getStatus(), appointmentId);
                businessMetrics.recordPaymentFailed();
                appointmentRepository.findById(appointmentId).ifPresent(appointment ->
                    eventPublisher.publishPaymentFailed(new PaymentFailedEvent(
                        UUID.randomUUID().toString(),
                        appointmentId,
                        appointment.getPatient().getId(),
                        appointment.getPatient().getUser().getName(),
                        appointment.getPatient().getUser().getEmail(),
                        appointment.getPaymentAmount(),
                        "BRL",
                        LocalDateTime.now().toString()
                    ))
                );
                return;
            }

            appointmentRepository.findById(appointmentId).ifPresent(appointment -> {
                appointment.setPaymentStatus(AppointmentPaymentStatus.PAID);
                appointment.setPaymentId(paymentId);
                appointmentRepository.save(appointment);
                businessMetrics.recordPaymentSucceeded();
                log.info("Consulta {} marcada como PAID via pagamento {}", appointmentId, paymentId);
                publishPaymentSucceeded(appointment, paymentId);
            });

        } catch (Exception e) {
            log.error("Erro ao processar webhook de pagamento: {}", e.getMessage());
        }
    }

    private void publishPaymentSucceeded(Appointment appointment, String paymentId) {
        try {
            eventPublisher.publishPaymentSucceeded(new PaymentSucceededEvent(
                UUID.randomUUID().toString(),
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getPatient().getUser().getName(),
                appointment.getPatient().getUser().getEmail(),
                appointment.getPaymentAmount(),
                "BRL",
                appointment.getChosenPaymentMethod() != null
                    ? appointment.getChosenPaymentMethod().name() : "MERCADOPAGO",
                paymentId,
                LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            log.error("Failed to publish PaymentSucceededEvent for appointment {}: {}", appointment.getId(), e.getMessage());
        }
    }
}
