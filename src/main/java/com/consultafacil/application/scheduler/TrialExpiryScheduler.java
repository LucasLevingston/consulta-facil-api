package com.consultafacil.application.scheduler;

import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.enums.SubscriptionPlan;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.port.out.subscription.SubscriptionRepositoryPort;
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
public class TrialExpiryScheduler {

    private final SubscriptionRepositoryPort subscriptionRepository;

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void expireTrials() {
        List<Subscription> expired = subscriptionRepository.findExpiredTrialsBefore(LocalDateTime.now());
        if (expired.isEmpty()) return;
        log.info("[TrialExpiry] Downgrading {} expired trial(s) to FREE", expired.size());
        for (Subscription sub : expired) {
            sub.setPlan(SubscriptionPlan.FREE);
            sub.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(sub);
            log.info("[TrialExpiry] Trial expired for userId={}", sub.getUser().getId());
        }
    }
}
