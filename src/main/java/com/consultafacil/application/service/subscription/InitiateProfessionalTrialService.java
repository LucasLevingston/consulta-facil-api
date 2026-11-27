package com.consultafacil.application.service.subscription;

import com.consultafacil.application.port.in.subscription.InitiateProfessionalTrialUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.SubscriptionPlan;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.port.out.subscription.SubscriptionRepositoryPort;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitiateProfessionalTrialService implements InitiateProfessionalTrialUseCase {

    private final SubscriptionRepositoryPort subscriptionRepository;
    private final UserRepositoryPort userRepository;

    @Override
    @Transactional
    public void execute(String userId) {
        // Idempotent: skip if subscription already exists
        if (subscriptionRepository.findByUserId(userId).isPresent()) {
            log.debug("[Trial] Subscription already exists for userId={}, skipping trial init", userId);
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Subscription trial = Subscription.builder()
                .user(user)
                .planId("free")
                .plan(SubscriptionPlan.FREE)
                .status(SubscriptionStatus.ACTIVE)
                .trialEndsAt(LocalDateTime.now().plusDays(14))
                .build();

        subscriptionRepository.save(trial);
        log.info("[Trial] 14-day PROFESSIONAL trial initiated for userId={}", userId);
    }
}
