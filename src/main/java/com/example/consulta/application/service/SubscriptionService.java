package com.example.consulta.application.service;

import com.example.consulta.api.dto.subscription.CheckoutResponseDTO;
import com.example.consulta.api.dto.subscription.SubscriptionResponseDTO;
import com.example.consulta.core.config.MercadoPagoConfig;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.Subscription;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.SubscriptionStatus;
import com.example.consulta.domain.port.out.EmailPort;
import com.example.consulta.domain.port.out.SubscriptionRepositoryPort;
import com.example.consulta.domain.port.out.UserRepositoryPort;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preapproval.PreApprovalAutoRecurringCreateRequest;
import com.mercadopago.client.preapproval.PreapprovalClient;
import com.mercadopago.client.preapproval.PreapprovalCreateRequest;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preapproval.Preapproval;
import com.example.consulta.application.port.in.SubscriptionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService implements SubscriptionUseCase {

    private final SubscriptionRepositoryPort subscriptionRepository;
    private final UserRepositoryPort userRepository;
    private final MercadoPagoConfig mpConfig;
    private final EmailPort emailPort;

    @Value("${app.url:http://localhost:3000}")
    private String appUrl;

    private static final Map<String, PlanInfo> PLANS = Map.of(
            "monthly",        new PlanInfo("Plano Pro Mensal",       new BigDecimal("149.90"),  1, "months"),
            "yearly",         new PlanInfo("Plano Pro Anual",        new BigDecimal("1618.92"), 12, "months"),
            "clinic-monthly", new PlanInfo("Plano Clínica Mensal",   new BigDecimal("700.00"),  1, "months"),
            "clinic-yearly",  new PlanInfo("Plano Clínica Anual",    new BigDecimal("7560.00"), 12, "months"));

    record PlanInfo(String title, BigDecimal price, int frequency, String frequencyType) {
        int durationDays() { return frequency == 12 ? 365 : 30; }
    }

    @Transactional
    public CheckoutResponseDTO createCheckout(String userId, String planId) {
        PlanInfo plan = PLANS.get(planId);
        if (plan == null) throw new IllegalArgumentException("Invalid plan: " + planId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        try {
            PreApprovalAutoRecurringCreateRequest autoRecurring = PreApprovalAutoRecurringCreateRequest.builder()
                    .currencyId("BRL")
                    .transactionAmount(plan.price())
                    .frequency(plan.frequency())
                    .frequencyType(plan.frequencyType())
                    .startDate(OffsetDateTime.now().plusSeconds(30))
                    .build();

            PreapprovalCreateRequest request = PreapprovalCreateRequest.builder()
                    .payerEmail(user.getEmail())
                    .reason(plan.title())
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
            subscriptionRepository.save(subscription);

            return CheckoutResponseDTO.builder()
                    .checkoutUrl(preapproval.getInitPoint())
                    .preferenceId(preapproval.getId())
                    .build();

        } catch (Exception e) {
            log.error("Erro ao criar preapproval MP para user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Erro ao criar checkout: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void handlePaymentApproved(String paymentId, String externalReference) {
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

        PlanInfo plan = PLANS.get(planId);
        if (plan == null) return;

        Optional<Subscription> opt = subscriptionRepository.findByUserId(userId);
        Subscription subscription = opt.orElseGet(() -> userRepository.findById(userId)
                .map(u -> Subscription.builder().user(u).planId(planId).build()).orElse(null));
        if (subscription == null) return;

        // Extend from current expiry if still active, otherwise from now
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

        sendRenewalEmail(subscription.getUser(), plan, newExpiry);
    }

    @Transactional
    public void handlePreapprovalWebhook(String preapprovalId) {
        try {
            Preapproval preapproval = new PreapprovalClient().get(preapprovalId);
            String status = preapproval.getStatus();

            subscriptionRepository.findByMpPreapprovalId(preapprovalId).ifPresentOrElse(sub -> {
                if ("cancelled".equals(status) || "paused".equals(status)) {
                    sub.setStatus(SubscriptionStatus.CANCELLED);
                    subscriptionRepository.save(sub);
                    log.info("[Subscription] Preapproval {} → CANCELLED for userId={}", preapprovalId, sub.getUser().getId());
                } else if ("authorized".equals(status) && sub.getStatus() == SubscriptionStatus.PENDING) {
                    // First authorization — activate immediately (payment webhook will extend expiry)
                    sub.setStatus(SubscriptionStatus.ACTIVE);
                    subscriptionRepository.save(sub);
                    log.info("[Subscription] Preapproval {} authorized for userId={}", preapprovalId, sub.getUser().getId());
                }
            }, () -> log.warn("[Subscription] Preapproval {} not found in DB", preapprovalId));

        } catch (Exception e) {
            log.error("[Subscription] Error processing preapproval {}: {}", preapprovalId, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Optional<SubscriptionResponseDTO> getMySubscription(String userId) {
        return subscriptionRepository.findByUserId(userId).map(this::toDTO);
    }

    private void sendRenewalEmail(User user, PlanInfo plan, LocalDateTime nextExpiry) {
        try {
            String nextDate = nextExpiry.format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")));
            emailPort.sendSubscriptionRenewed(
                user.getEmail(), user.getName(), plan.title(),
                plan.price().toPlainString(), nextDate);
        } catch (Exception e) {
            log.error("[Email] Failed to send renewal email for user {}: {}", user.getId(), e.getMessage());
        }
    }

    private SubscriptionResponseDTO toDTO(Subscription s) {
        return SubscriptionResponseDTO.builder()
                .id(s.getId())
                .planId(s.getPlanId())
                .status(s.getStatus())
                .expiresAt(s.getExpiresAt())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
