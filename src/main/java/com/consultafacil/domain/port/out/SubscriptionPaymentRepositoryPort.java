package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.SubscriptionPayment;

import java.time.LocalDateTime;
import java.util.List;

public interface SubscriptionPaymentRepositoryPort {
    SubscriptionPayment save(SubscriptionPayment payment);
    List<SubscriptionPayment> findByPaidAtBetween(LocalDateTime start, LocalDateTime end);
    List<SubscriptionPayment> findBySubscriptionId(String subscriptionId);
}
