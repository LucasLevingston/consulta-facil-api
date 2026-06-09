package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.subscription.CheckoutResponseDTO;
import com.consultafacil.api.dto.subscription.SubscriptionResponseDTO;

import java.util.Optional;

public interface SubscriptionUseCase {

    CheckoutResponseDTO createCheckout(String userId, String planId, String referralSlug, String couponCode);

    void handlePaymentApproved(String paymentId, String externalReference);

    Optional<SubscriptionResponseDTO> getMySubscription(String userId);

    void handlePreapprovalWebhook(String preapprovalId);
}
