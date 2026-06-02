package com.example.consulta.application.service;

import com.example.consulta.api.dto.subscription.CheckoutResponseDTO;
import com.example.consulta.api.dto.subscription.SubscriptionResponseDTO;
import com.example.consulta.core.config.MercadoPagoConfig;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.Subscription;
import com.example.consulta.domain.entity.User;
import com.example.consulta.domain.enums.SubscriptionStatus;
import com.example.consulta.domain.port.out.SubscriptionRepositoryPort;
import com.example.consulta.domain.port.out.UserRepositoryPort;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.example.consulta.application.port.in.SubscriptionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService implements SubscriptionUseCase {

    private final SubscriptionRepositoryPort subscriptionRepository;
    private final UserRepositoryPort userRepository;
    private final MercadoPagoConfig mpConfig;

    private static final Map<String, PlanInfo> PLANS = Map.of(
            "monthly", new PlanInfo("Plano Pro Mensal", new BigDecimal("49.90"), 30),
            "yearly", new PlanInfo("Plano Pro Anual", new BigDecimal("499.90"), 365),
            "clinic-monthly", new PlanInfo("Plano Clínica Mensal", new BigDecimal("149.90"), 30),
            "clinic-yearly", new PlanInfo("Plano Clínica Anual", new BigDecimal("1499.90"), 365));

    record PlanInfo(String title, BigDecimal price, int durationDays) {
    }

    @Transactional
    public CheckoutResponseDTO createCheckout(String userId, String planId) {
        PlanInfo plan = PLANS.get(planId);
        if (plan == null)
            throw new IllegalArgumentException("Plano inválido: " + planId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        try {
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title(plan.title())
                    .quantity(1)
                    .unitPrice(plan.price())
                    .currencyId("BRL")
                    .build();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(mpConfig.getSuccessUrl() + "?planId=" + planId)
                    .failure(mpConfig.getFailureUrl())
                    .pending(mpConfig.getPendingUrl())
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(List.of(item))
                    .backUrls(backUrls)
                    .autoReturn("approved")
                    .externalReference(userId + "|" + planId)
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // Persist pending subscription record
            Subscription subscription = subscriptionRepository.findByUserId(userId)
                    .orElse(Subscription.builder().user(user).build());
            subscription.setPlanId(planId);
            subscription.setStatus(SubscriptionStatus.PENDING);
            subscription.setMpPreferenceId(preference.getId());
            subscriptionRepository.save(subscription);

            return CheckoutResponseDTO.builder()
                    .checkoutUrl(preference.getInitPoint())
                    .preferenceId(preference.getId())
                    .build();

        } catch (Exception e) {
            log.error("Erro ao criar preferência MP para user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Erro ao criar checkout: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void handlePaymentApproved(String paymentId, String externalReference) {
        String ref = externalReference;
        if (ref == null) {
            try {
                Payment payment = new PaymentClient().get(Long.parseLong(paymentId));
                if (!"approved".equals(payment.getStatus()))
                    return;
                ref = payment.getExternalReference();
            } catch (Exception e) {
                log.error("Erro ao buscar pagamento MP {}: {}", paymentId, e.getMessage());
                return;
            }
        }
        if (ref == null || !ref.contains("|"))
            return;
        String externalRef = ref;

        String[] parts = externalRef.split("\\|");
        String userId = parts[0];
        String planId = parts[1];

        PlanInfo plan = PLANS.get(planId);
        if (plan == null)
            return;

        Optional<Subscription> opt = subscriptionRepository.findByUserId(userId);
        Subscription subscription = opt.orElseGet(() -> userRepository.findById(userId)
                .map(u -> Subscription.builder().user(u).planId(planId).build()).orElse(null));
        if (subscription == null)
            return;

        subscription.setPlanId(planId);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setMpPaymentId(paymentId);
        subscription.setExpiresAt(LocalDateTime.now().plusDays(plan.durationDays()));
        subscriptionRepository.save(subscription);

    }

    @Transactional(readOnly = true)
    public Optional<SubscriptionResponseDTO> getMySubscription(String userId) {
        return subscriptionRepository.findByUserId(userId).map(this::toDTO);
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
