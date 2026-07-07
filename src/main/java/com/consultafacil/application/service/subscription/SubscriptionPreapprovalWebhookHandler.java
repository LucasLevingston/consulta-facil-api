package com.consultafacil.application.service.subscription;

import com.consultafacil.application.port.in.HandlePreapprovalWebhookUseCase;
import com.consultafacil.application.port.in.RecordCouponUseUseCase;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.port.out.PlanRepositoryPort;
import com.consultafacil.domain.port.out.SubscriptionRepositoryPort;
import com.mercadopago.client.preapproval.PreapprovalClient;
import com.mercadopago.resources.preapproval.Preapproval;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionPreapprovalWebhookHandler implements HandlePreapprovalWebhookUseCase {

    private final SubscriptionRepositoryPort subscriptionRepository;
    private final PlanRepositoryPort planRepository;
    private final RecordCouponUseUseCase recordCouponUseUseCase;
    private final SubscriptionSellerSaleLinker sellerSaleLinker;
    private final SubscriptionPaymentRecorder paymentRecorder;

    @Override
    public void execute(String preapprovalId) {
        try {
            Preapproval preapproval = new PreapprovalClient().get(preapprovalId);
            String status = preapproval.getStatus();

            subscriptionRepository.findByMpPreapprovalId(preapprovalId).ifPresentOrElse(sub -> {
                if ("cancelled".equals(status) || "paused".equals(status)) {
                    sub.setStatus(SubscriptionStatus.CANCELLED);
                    subscriptionRepository.save(sub);
                    log.info("[Subscription] Preapproval {} → CANCELLED for userId={}", preapprovalId, sub.getUser().getId());
                } else if ("authorized".equals(status) && sub.getStatus() == SubscriptionStatus.PENDING) {
                    activate(sub, preapprovalId);
                }
            }, () -> log.warn("[Subscription] Preapproval {} not found in DB", preapprovalId));

        } catch (Exception e) {
            log.error("[Subscription] Error processing preapproval {}: {}", preapprovalId, e.getMessage());
        }
    }

    private void activate(com.consultafacil.domain.entity.Subscription sub, String preapprovalId) {
        sub.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(sub);
        log.info("[Subscription] Preapproval {} authorized for userId={}", preapprovalId, sub.getUser().getId());
        sellerSaleLinker.createSellerSaleIfPresent(sub);
        if (sub.getCouponId() != null) {
            recordCouponUseUseCase.execute(sub.getCouponId(), sub.getUser().getId(), sub.getId(), sub.getDiscountApplied());
        }
        planRepository.findBySlug(sub.getPlanId()).ifPresent(plan -> {
            BigDecimal gross = sub.getDiscountApplied() != null
                    ? plan.getPrice().subtract(sub.getDiscountApplied()).max(BigDecimal.ZERO)
                    : plan.getPrice();
            paymentRecorder.recordPayment(sub.getId(), preapprovalId, gross, "CREDIT_CARD");
        });
    }
}
