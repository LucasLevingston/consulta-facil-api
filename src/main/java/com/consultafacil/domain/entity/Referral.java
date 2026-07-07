package com.consultafacil.domain.entity;

import com.consultafacil.domain.enums.ReferralStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "referrals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Referral {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "referrer_id", nullable = false)
    private String referrerId;

    @Column(name = "referred_id", nullable = false, unique = true)
    private String referredId;

    @Column(name = "referral_code_id", nullable = false)
    private String referralCodeId;

    @Column(name = "first_payment_id")
    private String firstPaymentId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReferralStatus status = ReferralStatus.PENDING;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    private void validate() {
        if (referrerId != null && referrerId.equals(referredId)) {
            throw new IllegalArgumentException("Auto-indicação não é permitida");
        }
    }
}
