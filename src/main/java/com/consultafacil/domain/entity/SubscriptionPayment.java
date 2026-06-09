package com.consultafacil.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "subscription_id", nullable = false)
    private String subscriptionId;

    @Column(name = "mp_payment_id")
    private String mpPaymentId;

    @Column(name = "gross_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "processing_fee", precision = 10, scale = 2)
    private BigDecimal processingFee;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "iss_amount", precision = 10, scale = 2)
    private BigDecimal issAmount;

    @Column(name = "net_amount", precision = 10, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "tax_rate_applied", precision = 5, scale = 2)
    private BigDecimal taxRateApplied;

    @Column(name = "tax_regime", length = 30)
    private String taxRegime;

    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    @Column(name = "fiscal_document_id")
    private String fiscalDocumentId;

    @Column(name = "tax_snapshot", columnDefinition = "TEXT")
    private String taxSnapshot;

    @Column(name = "paid_at", nullable = false)
    @Builder.Default
    private LocalDateTime paidAt = LocalDateTime.now();
}
