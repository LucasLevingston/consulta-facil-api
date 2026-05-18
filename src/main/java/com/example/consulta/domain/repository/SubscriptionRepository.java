package com.example.consulta.domain.repository;

import com.example.consulta.domain.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {
    Optional<Subscription> findByUserId(String userId);
    Optional<Subscription> findByMpPreferenceId(String preferenceId);
    Optional<Subscription> findByMpPaymentId(String paymentId);
}
