package com.example.consulta.application.scheduler;

import com.example.consulta.domain.entity.Subscription;
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
public class SubscriptionRenewalReminderScheduler {

    private final SubscriptionRepositoryPort subscriptionRepository;
    private final EmailPort emailPort;

    @Value("${app.url:http://localhost:3000}")
    private String appUrl;

    private static final Map<String, String> PLAN_LABELS = Map.of(
            "monthly",        "Plano Pro Mensal",
            "yearly",         "Plano Pro Anual",
            "clinic-monthly", "Plano Clínica Mensal",
            "clinic-yearly",  "Plano Clínica Anual");

    /** Daily at 09:00 — notify users whose subscription expires in ~7 days. */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional(readOnly = true)
    public void sendRenewalReminders() {
        LocalDateTime from = LocalDateTime.now().plusDays(6);
        LocalDateTime to   = LocalDateTime.now().plusDays(8);
        List<Subscription> expiringSoon = subscriptionRepository.findActiveExpiringBetween(from, to);
        if (expiringSoon.isEmpty()) return;

        log.info("[RenewalReminder] Sending reminder for {} subscription(s)", expiringSoon.size());
        for (Subscription subscription : expiringSoon) {
            sendReminder(subscription);
        }
    }

    private void sendReminder(Subscription subscription) {
        try {
            String email = subscription.getUser().getEmail();
            String name  = subscription.getUser().getName();
            String label = PLAN_LABELS.getOrDefault(subscription.getPlanId(), subscription.getPlanId());
            String manageUrl = appUrl + "/planos";
            emailPort.sendSubscriptionRenewalReminder(email, name, label, 7, manageUrl);
            log.info("[RenewalReminder] Reminder sent for userId={}", subscription.getUser().getId());
        } catch (Exception e) {
            log.error("[RenewalReminder] Failed to send reminder for subscription {}: {}",
                subscription.getId(), e.getMessage());
        }
    }
}
