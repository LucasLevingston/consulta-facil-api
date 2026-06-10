package com.consultafacil.domain.repository;

import com.consultafacil.domain.entity.SubscriptionPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SubscriptionPaymentRepository extends JpaRepository<SubscriptionPayment, String> {
    List<SubscriptionPayment> findByPaidAtBetween(LocalDateTime start, LocalDateTime end);
    List<SubscriptionPayment> findBySubscriptionId(String subscriptionId);
    boolean existsByMpPaymentId(String mpPaymentId);
}
