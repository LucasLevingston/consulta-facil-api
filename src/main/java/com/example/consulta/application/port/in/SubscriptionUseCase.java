package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.subscription.CheckoutResponseDTO;
import com.example.consulta.api.dto.subscription.SubscriptionResponseDTO;

import java.util.Optional;

public interface SubscriptionUseCase {

    CheckoutResponseDTO createCheckout(String userId, String planId);

    void handlePaymentApproved(String paymentId, String externalReference);

    Optional<SubscriptionResponseDTO> getMySubscription(String userId);

    void handlePreapprovalWebhook(String preapprovalId);
}
