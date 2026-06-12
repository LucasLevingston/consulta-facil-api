package com.consultafacil.api.dto.billing.payment;

import com.consultafacil.domain.enums.BillingPaymentStatus;
import com.consultafacil.domain.enums.OwnerType;
import com.consultafacil.domain.enums.PaymentType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BillingPaymentResponseDTO {
    private String id;
    private PaymentType paymentType;
    private String referenceId;
    private OwnerType ownerType;
    private String ownerId;
    private BigDecimal amount;
    private BigDecimal systemFee;
    private BigDecimal gatewayFee;
    private BigDecimal netAmount;
    private String currency;
    private String paymentMethod;
    private String gateway;
    private String gatewayPaymentId;
    private BillingPaymentStatus status;
    private String payerId;
    private String payerName;
    private String payerEmail;
    private String description;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
