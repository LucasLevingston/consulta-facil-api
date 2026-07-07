package com.consultafacil.domain.port.out.subscription;

import com.consultafacil.domain.entity.Subscription;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepositoryPort {

    Subscription save(Subscription subscription);

    Optional<Subscription> findByUserId(String userId);

    Optional<Subscription> findByMpPreferenceId(String preferenceId);

    Optional<Subscription> findByMpPaymentId(String paymentId);

    Optional<Subscription> findByMpPreapprovalId(String mpPreapprovalId);

    List<Subscription> findActiveExpiredBefore(LocalDateTime now);

    List<Subscription> findActiveExpiringBetween(LocalDateTime from, LocalDateTime to);
}
