package com.consultafacil.application.service;

import com.consultafacil.api.dto.coupon.CouponValidationResponseDTO;
import com.consultafacil.api.dto.subscription.CheckoutResponseDTO;
import com.consultafacil.api.dto.subscription.SubscriptionResponseDTO;
import com.consultafacil.api.dto.tax.TaxBreakdown;
import com.consultafacil.application.port.in.CouponUseCase;
import com.consultafacil.application.port.in.SubscriptionUseCase;
import com.consultafacil.core.config.MercadoPagoConfig;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.entity.SellerSale;
import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.entity.SubscriptionPayment;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.SellerSaleStatus;
import com.consultafacil.domain.enums.SellerStatus;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.port.out.EmailPort;
import com.consultafacil.domain.port.out.PlanRepositoryPort;
import com.consultafacil.domain.port.out.SellerRepositoryPort;
import com.consultafacil.domain.port.out.SellerSaleRepositoryPort;
import com.consultafacil.domain.port.out.SubscriptionPaymentRepositoryPort;
import com.consultafacil.domain.port.out.SubscriptionRepositoryPort;
import com.consultafacil.domain.port.out.UserRepositoryPort;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preapproval.PreApprovalAutoRecurringCreateRequest;
import com.mercadopago.client.preapproval.PreapprovalClient;
import com.mercadopago.client.preapproval.PreapprovalCreateRequest;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preapproval.Preapproval;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService implements SubscriptionUseCase {

    private final SubscriptionRepositoryPort subscriptionRepository;
    private final UserRepositoryPort userRepository;
    private final PlanRepositoryPort planRepository;
    private final SellerRepositoryPort sellerRepository;
    private final SellerSaleRepositoryPort sellerSaleRepository;
    private final SubscriptionPaymentRepositoryPort subscriptionPaymentRepository;
    private final CouponUseCase couponUseCase;
    private final TaxCalculationService taxCalculationService;
    private final MercadoPagoConfig mpConfig;
    private final EmailPort emailPort;

    @Value("${app.url:http://localhost:3000}")
    private String appUrl;

    @Override
    @Transactional
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

    @Override
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

        recordPayment(subscription.getId(), paymentId, plan.getPrice(), "CREDIT_CARD");
        sendRenewalEmail(subscription.getUser(), plan, newExpiry);
    }

    @Override
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
                    sub.setStatus(SubscriptionStatus.ACTIVE);
                    subscriptionRepository.save(sub);
                    log.info("[Subscription] Preapproval {} authorized for userId={}", preapprovalId, sub.getUser().getId());
                    createSellerSaleIfPresent(sub);
                    if (sub.getCouponId() != null) {
                        couponUseCase.recordUse(sub.getCouponId(), sub.getUser().getId(),
                                sub.getId(), sub.getDiscountApplied());
                    }
                    planRepository.findBySlug(sub.getPlanId()).ifPresent(plan -> {
                        BigDecimal gross = sub.getDiscountApplied() != null
                                ? plan.getPrice().subtract(sub.getDiscountApplied()).max(BigDecimal.ZERO)
                                : plan.getPrice();
                        recordPayment(sub.getId(), preapprovalId, gross, "CREDIT_CARD");
                    });
                }
            }, () -> log.warn("[Subscription] Preapproval {} not found in DB", preapprovalId));

        } catch (Exception e) {
            log.error("[Subscription] Error processing preapproval {}: {}", preapprovalId, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SubscriptionResponseDTO> getMySubscription(String userId) {
        return subscriptionRepository.findByUserId(userId).map(this::toDTO);
    }

    // ── Seller commission linking ─────────────────────────────────────────

    private void createSellerSaleIfPresent(Subscription subscription) {
        String slug = subscription.getReferralSlug();
        if (slug == null || slug.isBlank()) return;

        if (sellerSaleRepository.findBySubscriptionId(subscription.getId()).isPresent()) return;

        sellerRepository.findBySlug(slug).ifPresentOrElse(seller -> {
            if (seller.getStatus() != SellerStatus.ACTIVE) {
                log.warn("[Seller] Slug {} found but seller is INACTIVE — skipping sale", slug);
                return;
            }

            Plan plan = planRepository.findBySlug(subscription.getPlanId()).orElse(null);
            BigDecimal grossAmount = plan != null ? plan.getPrice() : BigDecimal.ZERO;
            BigDecimal commissionAmount = grossAmount
                    .multiply(seller.getCommissionRate())
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

            SellerSale sale = SellerSale.builder()
                    .seller(seller)
                    .subscription(subscription)
                    .grossAmount(grossAmount)
                    .commissionAmount(commissionAmount)
                    .monthReference(LocalDate.now().withDayOfMonth(1))
                    .status(SellerSaleStatus.PENDING)
                    .build();

            sellerSaleRepository.save(sale);
            subscription.setSellerId(seller.getId());
            subscriptionRepository.save(subscription);

            log.info("[Seller] Sale created for sellerId={} subscriptionId={} commission=R${}",
                    seller.getId(), subscription.getId(), commissionAmount);
        }, () -> log.warn("[Seller] Referral slug '{}' not found — no sale created", slug));
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void recordPayment(String subscriptionId, String mpPaymentId,
                               BigDecimal grossAmount, String paymentMethod) {
        try {
            if (subscriptionPaymentRepository.existsByMpPaymentId(mpPaymentId)) {
                log.info("[Tax] Payment {} already recorded — skipping duplicate", mpPaymentId);
                return;
            }
            TaxBreakdown tax = taxCalculationService.calculate(grossAmount, paymentMethod);
            SubscriptionPayment payment = SubscriptionPayment.builder()
                    .subscriptionId(subscriptionId)
                    .mpPaymentId(mpPaymentId)
                    .grossAmount(tax.grossAmount())
                    .processingFee(tax.processingFee())
                    .taxAmount(tax.taxAmount())
                    .issAmount(tax.issAmount())
                    .netAmount(tax.netAmount())
                    .taxRateApplied(tax.taxRateApplied())
                    .taxRegime(tax.taxRegime())
                    .paymentMethod(tax.paymentMethod())
                    .taxSnapshot(taxCalculationService.buildSnapshot(tax))
                    .build();
            subscriptionPaymentRepository.save(payment);
            log.info("[Tax] Payment recorded subscriptionId={} gross={} net={}",
                    subscriptionId, grossAmount, tax.netAmount());
        } catch (Exception e) {
            log.error("[Tax] Failed to record payment for subscriptionId={}: {}", subscriptionId, e.getMessage());
        }
    }

    private void sendRenewalEmail(User user, Plan plan, LocalDateTime nextExpiry) {
        try {
            String nextDate = nextExpiry.format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")));
            emailPort.sendSubscriptionRenewed(
                user.getEmail(), user.getName(), plan.getName(),
                plan.getPrice().toPlainString(), nextDate);
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
