package com.consultafacil.application.service.billing;

import com.consultafacil.api.dto.billing.payment.BillingPaymentResponseDTO;
import com.consultafacil.domain.entity.BillingPayment;
import org.springframework.stereotype.Component;

@Component
public class BillingPaymentMapper {

    public BillingPaymentResponseDTO toDTO(BillingPayment p) {
        return BillingPaymentResponseDTO.builder()
                .id(p.getId())
                .paymentType(p.getPaymentType())
                .referenceId(p.getReferenceId())
                .ownerType(p.getOwnerType())
                .ownerId(p.getOwnerId())
                .amount(p.getAmount())
                .systemFee(p.getSystemFee())
                .gatewayFee(p.getGatewayFee())
                .netAmount(p.getNetAmount())
                .currency(p.getCurrency())
                .paymentMethod(p.getPaymentMethod())
                .gateway(p.getGateway())
                .gatewayPaymentId(p.getGatewayPaymentId())
                .status(p.getStatus())
                .payerId(p.getPayerId())
                .payerName(p.getPayerName())
                .payerEmail(p.getPayerEmail())
                .description(p.getDescription())
                .paidAt(p.getPaidAt())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
