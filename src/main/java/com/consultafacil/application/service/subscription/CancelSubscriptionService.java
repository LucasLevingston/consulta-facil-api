package com.consultafacil.application.service.subscription;

import com.consultafacil.application.port.in.subscription.CancelSubscriptionUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.exception.InvalidStateException;
import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.port.out.subscription.SubscriptionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CancelSubscriptionService implements CancelSubscriptionUseCase {

    private final SubscriptionRepositoryPort subscriptionRepository;

    @Override
    @Transactional
    public void execute(String userId) {
        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", userId));

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new InvalidStateException("ALREADY_CANCELLED: Assinatura já está cancelada");
        }
        if (subscription.getStatus() == SubscriptionStatus.EXPIRED) {
            throw new InvalidStateException("ALREADY_EXPIRED: Assinatura já expirou");
        }

        subscription.setCancelAtPeriodEnd(true);
        subscriptionRepository.save(subscription);
        log.info("[Subscription] Cancel at period end set for userId={}", userId);
    }
}
