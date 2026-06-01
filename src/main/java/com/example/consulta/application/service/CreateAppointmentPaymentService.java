package com.example.consulta.application.service;

import com.example.consulta.api.dto.appointment.PaymentResponseDTO;
import com.example.consulta.core.config.MercadoPagoConfig;
import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.Appointment;
import com.example.consulta.domain.enums.AppointmentPaymentStatus;
import com.example.consulta.domain.enums.AppointmentStatus;
import com.example.consulta.domain.enums.PaymentMethod;
import com.example.consulta.domain.repository.AppointmentRepository;
import com.example.consulta.domain.repository.PatientProfileRepository;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import com.example.consulta.application.port.in.CreateAppointmentPaymentUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateAppointmentPaymentService implements CreateAppointmentPaymentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final MercadoPagoConfig mpConfig;

    @Transactional
    public PaymentResponseDTO execute(String appointmentId, String patientUserId, BigDecimal amount) {
        var patientProfile = patientProfileRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("PatientProfile", patientUserId));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        if (!appointment.getPatient().getId().equals(patientProfile.getId())) {
            throw new BadRequestException("Appointment does not belong to this patient");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELED
                || appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Cannot pay for a canceled or completed appointment");
        }

        if (appointment.getPaymentStatus() == AppointmentPaymentStatus.PAID) {
            throw new BadRequestException("Appointment is already paid");
        }

        var acceptedMethods = appointment.getProfessional().getAcceptedPaymentMethods();
        if (!acceptedMethods.isEmpty() && !acceptedMethods.contains(PaymentMethod.MERCADOPAGO)) {
            throw new BadRequestException("Este profissional não aceita pagamento via MercadoPago");
        }

        BigDecimal paymentAmount = amount != null ? amount : new BigDecimal("0.01");

        try {
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title("Consulta — " + appointment.getProfessional().getSpecialty())
                    .description("Agendamento em " + appointment.getScheduledAt())
                    .quantity(1)
                    .unitPrice(paymentAmount)
                    .currencyId("BRL")
                    .build();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(mpConfig.getSuccessUrl() + "?appointmentId=" + appointmentId)
                    .failure(mpConfig.getFailureUrl() + "?appointmentId=" + appointmentId)
                    .pending(mpConfig.getPendingUrl() + "?appointmentId=" + appointmentId)
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(List.of(item))
                    .backUrls(backUrls)
                    .autoReturn("approved")
                    .externalReference("appointment|" + appointmentId)
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            appointment.setPaymentStatus(AppointmentPaymentStatus.PENDING_PAYMENT);
            appointment.setPaymentAmount(paymentAmount);
            appointment.setPaymentPreferenceId(preference.getId());
            appointmentRepository.save(appointment);

            return PaymentResponseDTO.builder()
                    .checkoutUrl(preference.getInitPoint())
                    .preferenceId(preference.getId())
                    .appointmentId(appointmentId)
                    .build();

        } catch (Exception e) {
            log.error("Erro ao criar preferência de pagamento para consulta {}: {}", appointmentId, e.getMessage());
            throw new RuntimeException("Erro ao iniciar pagamento: " + e.getMessage(), e);
        }
    }
}
