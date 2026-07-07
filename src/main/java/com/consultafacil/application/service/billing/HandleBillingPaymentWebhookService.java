package com.consultafacil.application.service.billing;

import com.consultafacil.api.dto.billing.payment.BillingPaymentResponseDTO;
import com.consultafacil.application.port.in.HandleBillingPaymentWebhookUseCase;
import com.consultafacil.application.port.in.HandlePaymentPaidCommissionUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.BillingPayment;
import com.consultafacil.domain.enums.BillingPaymentStatus;
import com.consultafacil.domain.port.out.BillingPaymentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class HandleBillingPaymentWebhookService implements HandleBillingPaymentWebhookUseCase {

    private final BillingPaymentRepositoryPort paymentRepository;
    private final HandlePaymentPaidCommissionUseCase handlePaymentPaidCommissionUseCase;
    private final BillingPaymentMapper mapper;

    @Override
    @Transactional
    public BillingPaymentResponseDTO execute(String gatewayPaymentId, String newStatus) {
        BillingPayment payment = paymentRepository.findByGatewayPaymentId(gatewayPaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("BillingPayment", gatewayPaymentId));
        try {
            payment.setStatus(BillingPaymentStatus.valueOf(newStatus.toUpperCase()));
        } catch (IllegalArgumentException ignored) {
        }
        boolean wasPending = payment.getStatus() != BillingPaymentStatus.PAID;
        if (payment.getStatus() == BillingPaymentStatus.PAID && payment.getPaidAt() == null) {
            payment.setPaidAt(LocalDateTime.now());
        }
        BillingPayment saved = paymentRepository.save(payment);
        if (wasPending && saved.getStatus() == BillingPaymentStatus.PAID && saved.getPayerId() != null) {
            handlePaymentPaidCommissionUseCase.execute(saved.getId(), saved.getAmount(), saved.getPayerId());
        }
        return mapper.toDTO(saved);
    }
}
