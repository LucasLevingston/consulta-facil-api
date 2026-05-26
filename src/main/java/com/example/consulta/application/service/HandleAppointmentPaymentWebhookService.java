package com.example.consulta.application.service;

import com.example.consulta.application.observability.BusinessMetrics;
import com.example.consulta.domain.enums.AppointmentPaymentStatus;
import com.example.consulta.domain.repository.AppointmentRepository;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HandleAppointmentPaymentWebhookService {

    private final AppointmentRepository appointmentRepository;
    private final BusinessMetrics businessMetrics;

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
                return;
            }

            appointmentRepository.findById(appointmentId).ifPresent(appointment -> {
                appointment.setPaymentStatus(AppointmentPaymentStatus.PAID);
                appointment.setPaymentId(paymentId);
                appointmentRepository.save(appointment);
                businessMetrics.recordPaymentSucceeded();
                log.info("Consulta {} marcada como PAID via pagamento {}", appointmentId, paymentId);
            });

        } catch (Exception e) {
            log.error("Erro ao processar webhook de pagamento: {}", e.getMessage());
        }
    }
}
