package com.consultafacil.application.service;

import com.consultafacil.api.dto.appointment.PaymentResponseDTO;
import com.consultafacil.core.config.MercadoPagoConfig;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.enums.AppointmentPaymentStatus;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.enums.PaymentMethod;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import com.consultafacil.application.port.in.CreateAppointmentPaymentUseCase;
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

    private final AppointmentRepositoryPort appointmentRepository;
    private final PatientProfileRepositoryPort patientProfileRepository;
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
            throw new BadRequestException("This professional does not accept payment via MercadoPago");
        }

        // Derive amount: caller-supplied → appointment (set at scheduling) → professional price
        BigDecimal paymentAmount = amount;
        if (paymentAmount == null) paymentAmount = appointment.getPaymentAmount();
        if (paymentAmount == null) paymentAmount = appointment.getProfessional().getConsultationPrice();
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invalid payment amount. Configure the professional's consultation price.");
        }

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
