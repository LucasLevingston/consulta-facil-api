package com.consultafacil.adapter.out.persistence.subscription;

import com.consultafacil.domain.entity.SubscriptionPayment;
import com.consultafacil.domain.port.out.subscription.SubscriptionPaymentRepositoryPort;
import com.consultafacil.domain.repository.subscription.SubscriptionPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubscriptionPaymentRepositoryAdapter implements SubscriptionPaymentRepositoryPort {

    private final SubscriptionPaymentRepository repository;

    @Override
    public SubscriptionPayment save(SubscriptionPayment payment) {
        return repository.save(payment);
    }

    @Override
    public List<SubscriptionPayment> findByPaidAtBetween(LocalDateTime start, LocalDateTime end) {
        return repository.findByPaidAtBetween(start, end);
    }

    @Override
    public List<SubscriptionPayment> findBySubscriptionId(String subscriptionId) {
        return repository.findBySubscriptionId(subscriptionId);
    }

    @Override
    public boolean existsByMpPaymentId(String mpPaymentId) {
        return repository.existsByMpPaymentId(mpPaymentId);
    }
}
