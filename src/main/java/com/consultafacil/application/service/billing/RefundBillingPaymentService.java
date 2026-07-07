package com.consultafacil.application.service.billing;

import com.consultafacil.api.dto.billing.payment.BillingPaymentResponseDTO;
import com.consultafacil.application.port.in.CancelCommissionUseCase;
import com.consultafacil.application.port.in.RefundBillingPaymentUseCase;
import com.consultafacil.domain.entity.BillingPayment;
import com.consultafacil.domain.port.out.BillingPaymentRepositoryPort;
import com.consultafacil.domain.port.out.PaymentGatewayPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundBillingPaymentService implements RefundBillingPaymentUseCase {

    private final BillingPaymentRepositoryPort paymentRepository;
    private final PaymentGatewayPort paymentGateway;
    private final CancelCommissionUseCase cancelCommissionUseCase;
    private final BillingPaymentFinder finder;
    private final BillingPaymentMapper mapper;

    @Override
    @Transactional
    public BillingPaymentResponseDTO execute(String id) {
        BillingPayment payment = finder.findOrThrow(id);
        BillingPayment refunded = paymentGateway.refundPayment(payment.getGatewayPaymentId(), payment.getAmount());
        payment.setStatus(refunded.getStatus());
        BillingPayment saved = paymentRepository.save(payment);
        cancelCommissionUseCase.execute(saved.getId());
        return mapper.toDTO(saved);
    }
}
