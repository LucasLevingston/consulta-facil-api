package com.consultafacil.application.service.billing;

import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.BillingPayment;
import com.consultafacil.domain.port.out.billing.BillingPaymentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BillingPaymentFinder {

    private final BillingPaymentRepositoryPort paymentRepository;

    public BillingPayment findOrThrow(String id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BillingPayment", id));
    }
}
