package com.example.consulta.adapter.out.persistence;

import com.example.consulta.domain.entity.Subscription;
import com.example.consulta.domain.port.out.SubscriptionRepositoryPort;
import com.example.consulta.domain.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SubscriptionRepositoryAdapter implements SubscriptionRepositoryPort {

    private final SubscriptionRepository subscriptionRepository;

    @Override
    public Subscription save(Subscription subscription) {
        return subscriptionRepository.save(subscription);
    }

    @Override
    public Optional<Subscription> findByUserId(String userId) {
        return subscriptionRepository.findByUserId(userId);
    }

    @Override
    public Optional<Subscription> findByMpPreferenceId(String preferenceId) {
        return subscriptionRepository.findByMpPreferenceId(preferenceId);
    }

    @Override
    public Optional<Subscription> findByMpPaymentId(String paymentId) {
        return subscriptionRepository.findByMpPaymentId(paymentId);
    }

    @Override
    public Optional<Subscription> findByMpPreapprovalId(String mpPreapprovalId) {
        return subscriptionRepository.findByMpPreapprovalId(mpPreapprovalId);
    }

    @Override
    public List<Subscription> findActiveExpiredBefore(LocalDateTime now) {
        return subscriptionRepository.findActiveExpiredBefore(now);
    }

    @Override
    public List<Subscription> findActiveExpiringBetween(LocalDateTime from, LocalDateTime to) {
        return subscriptionRepository.findActiveExpiringBetween(from, to);
    }
}
