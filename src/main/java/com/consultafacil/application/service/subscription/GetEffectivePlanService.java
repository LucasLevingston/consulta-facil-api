package com.consultafacil.application.service.subscription;

import com.consultafacil.domain.enums.SubscriptionPlan;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.port.out.subscription.SubscriptionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GetEffectivePlanService {

    private final SubscriptionRepositoryPort subscriptionRepository;

    public SubscriptionPlan execute(String userId) {
        return subscriptionRepository.findByUserId(userId)
                .map(sub -> {
                    if (sub.getTrialEndsAt() != null && sub.getTrialEndsAt().isAfter(LocalDateTime.now())) {
                        return SubscriptionPlan.PROFESSIONAL;
                    }
                    if (sub.getStatus() == SubscriptionStatus.ACTIVE || sub.getStatus() == SubscriptionStatus.PENDING) {
                        return sub.getPlan();
                    }
                    return SubscriptionPlan.FREE;
                })
                .orElse(SubscriptionPlan.FREE);
    }
}
