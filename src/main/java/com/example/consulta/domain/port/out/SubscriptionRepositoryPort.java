package com.example.consulta.domain.port.out;

import com.example.consulta.domain.entity.Subscription;

import java.util.Optional;

public interface SubscriptionRepositoryPort {

    Subscription save(Subscription subscription);

    Optional<Subscription> findByUserId(String userId);

    Optional<Subscription> findByMpPreferenceId(String preferenceId);

    Optional<Subscription> findByMpPaymentId(String paymentId);
}
