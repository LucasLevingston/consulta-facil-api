package com.consultafacil.application.service;

import com.consultafacil.api.dto.coupon.CouponValidationResponseDTO;
import com.consultafacil.api.dto.subscription.CheckoutResponseDTO;
import com.consultafacil.application.port.in.CouponUseCase;
import com.consultafacil.core.config.MercadoPagoConfig;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.port.out.PlanRepositoryPort;
import com.consultafacil.domain.port.out.SubscriptionRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import com.mercadopago.client.preapproval.PreApprovalAutoRecurringCreateRequest;
import com.mercadopago.client.preapproval.PreapprovalClient;
import com.mercadopago.client.preapproval.PreapprovalCreateRequest;
import com.mercadopago.resources.preapproval.Preapproval;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionCheckoutService {

    private final PlanRepositoryPort planRepository;
    private final UserRepositoryPort userRepository;
    private final SubscriptionRepositoryPort subscriptionRepository;
    private final CouponUseCase couponUseCase;
    private final MercadoPagoConfig mpConfig;

    public CheckoutResponseDTO createCheckout(String userId, String planId, String referralSlug, String couponCode) {
        Plan plan = planRepository.findBySlug(planId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid plan: " + planId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        BigDecimal finalPrice = plan.getPrice();
        String couponId = null;
        BigDecimal discountApplied = null;

        if (couponCode != null && !couponCode.isBlank()) {
            CouponValidationResponseDTO coupon = couponUseCase.validate(couponCode, userId, planId, plan.getPrice());
            finalPrice = coupon.getFinalPrice();
            couponId = coupon.getCouponId();
            discountApplied = coupon.getDiscountAmount();
        }

        try {
            PreApprovalAutoRecurringCreateRequest autoRecurring = PreApprovalAutoRecurringCreateRequest.builder()
                    .currencyId("BRL")
                    .transactionAmount(finalPrice)
                    .frequency(plan.getFrequency())
                    .frequencyType(plan.getFrequencyType())
                    .startDate(OffsetDateTime.now().plusSeconds(30))
                    .build();

            PreapprovalCreateRequest request = PreapprovalCreateRequest.builder()
                    .payerEmail(user.getEmail())
                    .reason(plan.getName())
                    .externalReference(userId + "|" + planId)
                    .backUrl(mpConfig.getSuccessUrl() + "?planId=" + planId)
                    .autoRecurring(autoRecurring)
                    .build();

            Preapproval preapproval = new PreapprovalClient().create(request);

            Subscription subscription = subscriptionRepository.findByUserId(userId)
                    .orElse(Subscription.builder().user(user).build());
            subscription.setPlanId(planId);
            subscription.setStatus(SubscriptionStatus.PENDING);
            subscription.setMpPreapprovalId(preapproval.getId());
            if (referralSlug != null && !referralSlug.isBlank()) {
                subscription.setReferralSlug(referralSlug.trim().toUpperCase());
            }
            subscription.setCouponId(couponId);
            subscription.setDiscountApplied(discountApplied);
            subscriptionRepository.save(subscription);

            return CheckoutResponseDTO.builder()
                    .checkoutUrl(preapproval.getInitPoint())
                    .preferenceId(preapproval.getId())
                    .build();

        } catch (Exception e) {
            log.error("Erro ao criar preapproval MP para user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Erro ao processar checkout. Tente novamente.", e);
        }
    }
}
