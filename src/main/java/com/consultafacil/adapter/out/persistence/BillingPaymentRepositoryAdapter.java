package com.consultafacil.adapter.out.persistence;

import com.consultafacil.domain.entity.BillingPayment;
import com.consultafacil.domain.enums.OwnerType;
import com.consultafacil.domain.port.out.BillingPaymentRepositoryPort;
import com.consultafacil.domain.repository.BillingPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BillingPaymentRepositoryAdapter implements BillingPaymentRepositoryPort {

    private final BillingPaymentRepository billingPaymentRepository;

    @Override
    public BillingPayment save(BillingPayment payment) { return billingPaymentRepository.save(payment); }

    @Override
    public Optional<BillingPayment> findById(String id) { return billingPaymentRepository.findById(id); }

    @Override
    public List<BillingPayment> findByOwnerId(OwnerType ownerType, String ownerId) {
        return billingPaymentRepository.findByOwnerTypeAndOwnerId(ownerType, ownerId);
    }

    @Override
    public List<BillingPayment> findByPayerId(String payerId) {
        return billingPaymentRepository.findByPayerIdOrderByCreatedAtDesc(payerId);
    }

    @Override
    public Optional<BillingPayment> findByGatewayPaymentId(String gatewayPaymentId) {
        return billingPaymentRepository.findByGatewayPaymentId(gatewayPaymentId);
    }

    @Override
    public List<BillingPayment> findAll() { return billingPaymentRepository.findAll(); }
}
