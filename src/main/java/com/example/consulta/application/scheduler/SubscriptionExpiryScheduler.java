package com.example.consulta.application.scheduler;

import com.example.consulta.domain.entity.Subscription;
import com.example.consulta.domain.enums.SubscriptionStatus;
import com.example.consulta.domain.port.out.EmailPort;
import com.example.consulta.domain.port.out.SubscriptionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionExpiryScheduler {

    private final SubscriptionRepositoryPort subscriptionRepository;
    private final EmailPort emailPort;

    @Value("${app.url:http://localhost:3000}")
    private String appUrl;

    private static final Map<String, String> PLAN_LABELS = Map.of(
            "monthly",        "Plano Pro Mensal",
            "yearly",         "Plano Pro Anual",
            "clinic-monthly", "Plano Clínica Mensal",
            "clinic-yearly",  "Plano Clínica Anual");

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expireSubscriptions() {
        List<Subscription> expired = subscriptionRepository.findActiveExpiredBefore(LocalDateTime.now());
        if (expired.isEmpty()) return;

        log.info("[SubscriptionExpiry] Expiring {} subscription(s)", expired.size());
        for (Subscription subscription : expired) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);
            log.info("[SubscriptionExpiry] Subscription {} expired for userId={}",
                subscription.getId(), subscription.getUser().getId());
            sendExpiryEmail(subscription);
        }
    }

    private void sendExpiryEmail(Subscription subscription) {
        try {
            String email = subscription.getUser().getEmail();
            String name  = subscription.getUser().getName();
            String label = PLAN_LABELS.getOrDefault(subscription.getPlanId(), subscription.getPlanId());
            String renewUrl = appUrl + "/planos";
            emailPort.sendSubscriptionExpired(email, name, label, renewUrl);
        } catch (Exception e) {
            log.error("[SubscriptionExpiry] Failed to send expiry email for subscription {}: {}",
                subscription.getId(), e.getMessage());
        }
    }
}
