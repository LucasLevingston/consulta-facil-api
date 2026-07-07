package com.consultafacil.domain.entity;

import com.consultafacil.domain.enums.BillingPaymentStatus;
import com.consultafacil.domain.enums.OwnerType;
import com.consultafacil.domain.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "billing_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 30)
    private PaymentType paymentType;

    @Column(name = "reference_id", length = 36)
    private String referenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", length = 20)
    private OwnerType ownerType;

    @Column(name = "owner_id", length = 36)
    private String ownerId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "system_fee", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal systemFee = BigDecimal.ZERO;

    @Column(name = "gateway_fee", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal gatewayFee = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal netAmount;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "BRL";

    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String gateway = "MOCK";

    @Column(name = "gateway_payment_id", length = 100)
    private String gatewayPaymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BillingPaymentStatus status = BillingPaymentStatus.PENDING;

    @Column(name = "payer_id", length = 36)
    private String payerId;

    @Column(name = "payer_name", length = 150)
    private String payerName;

    @Column(name = "payer_email", length = 200)
    private String payerEmail;

    @Column(length = 300)
    private String description;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (netAmount == null) {
            netAmount = amount.subtract(systemFee).subtract(gatewayFee);
        }
    }
}
