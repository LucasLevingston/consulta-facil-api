package com.consultafacil.application.service;

import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.port.out.PlanRepositoryPort;
import com.consultafacil.domain.port.out.SubscriptionRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionPaymentApprovedHandler {

    private final SubscriptionRepositoryPort subscriptionRepository;
    private final UserRepositoryPort userRepository;
    private final PlanRepositoryPort planRepository;
    private final SubscriptionPaymentRecorder paymentRecorder;
    private final SubscriptionRenewalNotifier renewalNotifier;

    public void handle(String paymentId, String externalReference) {
        String ref = externalReference;
        if (ref == null) {
            try {
                Payment payment = new PaymentClient().get(Long.parseLong(paymentId));
                if (!"approved".equals(payment.getStatus())) return;
                ref = payment.getExternalReference();
            } catch (Exception e) {
                log.error("Erro ao buscar pagamento MP {}: {}", paymentId, e.getMessage());
                return;
            }
        }
        if (ref == null || !ref.contains("|")) return;

        String[] parts = ref.split("\\|");
        String userId = parts[0];
        String planId = parts[1];

        Plan plan = planRepository.findBySlug(planId).orElse(null);
        if (plan == null) return;

        Optional<Subscription> opt = subscriptionRepository.findByUserId(userId);
        Subscription subscription = opt.orElseGet(() -> userRepository.findById(userId)
                .map(u -> Subscription.builder().user(u).planId(planId).build()).orElse(null));
        if (subscription == null) return;

        LocalDateTime base = (subscription.getExpiresAt() != null
                && subscription.getExpiresAt().isAfter(LocalDateTime.now()))
                ? subscription.getExpiresAt()
                : LocalDateTime.now();
        LocalDateTime newExpiry = base.plusDays(plan.durationDays());

        subscription.setPlanId(planId);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setMpPaymentId(paymentId);
        subscription.setExpiresAt(newExpiry);
        subscriptionRepository.save(subscription);
        log.info("[Subscription] Renewed for userId={} plan={} expiresAt={}", userId, planId, newExpiry);

        paymentRecorder.recordPayment(subscription.getId(), paymentId, plan.getPrice(), "CREDIT_CARD");
        renewalNotifier.sendRenewalEmail(subscription.getUser(), plan, newExpiry);
    }
}
