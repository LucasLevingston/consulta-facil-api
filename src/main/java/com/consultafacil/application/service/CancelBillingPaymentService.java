package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.payment.BillingPaymentResponseDTO;
import com.consultafacil.application.port.in.CancelBillingPaymentUseCase;
import com.consultafacil.domain.entity.BillingPayment;
import com.consultafacil.domain.port.out.BillingPaymentRepositoryPort;
import com.consultafacil.domain.port.out.PaymentGatewayPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelBillingPaymentService implements CancelBillingPaymentUseCase {

    private final BillingPaymentRepositoryPort paymentRepository;
    private final PaymentGatewayPort paymentGateway;
    private final BillingPaymentFinder finder;
    private final BillingPaymentMapper mapper;

    @Override
    @Transactional
    public BillingPaymentResponseDTO execute(String id) {
        BillingPayment payment = finder.findOrThrow(id);
        BillingPayment cancelled = paymentGateway.cancelPayment(payment.getGatewayPaymentId());
        payment.setStatus(cancelled.getStatus());
        return mapper.toDTO(paymentRepository.save(payment));
    }
}
