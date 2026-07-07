package com.consultafacil.application.service;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.api.dto.appointment.PaymentResponseDTO;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.enums.PaymentMethod;
import com.consultafacil.domain.enums.PaymentTiming;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// Failures are swallowed: scheduling must succeed even if checkout generation fails.
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentCheckoutInitiator {

    private final CreateAppointmentPaymentService createAppointmentPaymentService;

    public void maybeInitiateCheckout(Appointment saved, ProfessionalProfile professional, String userId,
                                       BigDecimal paymentAmount, PaymentMethod chosenMethod,
                                       AppointmentResponseDTO response) {
        boolean shouldGenerate = professional.getPaymentTiming() == PaymentTiming.AT_SCHEDULING
                && chosenMethod == PaymentMethod.MERCADOPAGO
                && paymentAmount != null;
        if (!shouldGenerate) {
            return;
        }
        try {
            PaymentResponseDTO checkout = createAppointmentPaymentService.execute(
                    saved.getId(), userId, paymentAmount);
            response.setCheckoutUrl(checkout.getCheckoutUrl());
        } catch (Exception e) {
            log.error("Failed to auto-generate checkout for appointment {}: {}",
                    saved.getId(), e.getMessage());
        }
    }
}
