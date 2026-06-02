package com.example.consulta.application.scheduler;

import com.example.consulta.domain.entity.Subscription;
import com.example.consulta.domain.enums.SubscriptionStatus;
import com.example.consulta.domain.port.out.SubscriptionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionExpiryScheduler {

    private final SubscriptionRepositoryPort subscriptionRepository;

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
        }
    }
}
