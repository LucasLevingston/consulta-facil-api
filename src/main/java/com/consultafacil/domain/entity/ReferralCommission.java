package com.consultafacil.domain.entity;

import com.consultafacil.domain.enums.CommissionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "referral_commissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralCommission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "referral_id", nullable = false)
    private String referralId;

    @Column(name = "payment_id", nullable = false, unique = true)
    private String paymentId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Builder.Default
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage = BigDecimal.TEN;

    @Column(name = "available_at", nullable = false)
    private LocalDateTime availableAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommissionStatus status = CommissionStatus.PENDING;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
