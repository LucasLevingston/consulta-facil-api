package com.consultafacil.application.service;

import com.consultafacil.api.dto.subscription.CheckoutResponseDTO;
import com.consultafacil.api.dto.subscription.SubscriptionResponseDTO;
import com.consultafacil.application.port.in.SubscriptionUseCase;
import com.consultafacil.domain.port.out.SubscriptionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService implements SubscriptionUseCase {

    private final SubscriptionRepositoryPort subscriptionRepository;
    private final SubscriptionCheckoutService checkoutService;
    private final SubscriptionPaymentApprovedHandler paymentApprovedHandler;
    private final SubscriptionPreapprovalWebhookHandler preapprovalWebhookHandler;
    private final SubscriptionMapper mapper;

    @Override
    @Transactional
    public CheckoutResponseDTO createCheckout(String userId, String planId, String referralSlug, String couponCode) {
        return checkoutService.createCheckout(userId, planId, referralSlug, couponCode);
    }

    @Override
    @Transactional
    public void handlePaymentApproved(String paymentId, String externalReference) {
        paymentApprovedHandler.handle(paymentId, externalReference);
    }

    @Override
    @Transactional
    public void handlePreapprovalWebhook(String preapprovalId) {
        preapprovalWebhookHandler.handle(preapprovalId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SubscriptionResponseDTO> getMySubscription(String userId) {
        return subscriptionRepository.findByUserId(userId).map(mapper::toDTO);
    }
}
