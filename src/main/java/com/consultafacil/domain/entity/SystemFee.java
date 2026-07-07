package com.consultafacil.domain.entity;

import com.consultafacil.domain.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_fees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemFee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, unique = true, length = 30)
    private PaymentType paymentType;

    @Column(name = "fixed_fee", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal fixedFee = BigDecimal.ZERO;

    @Column(name = "percentage_fee", nullable = false, precision = 6, scale = 5)
    @Builder.Default
    private BigDecimal percentageFee = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public BigDecimal calculate(BigDecimal amount) {
        return fixedFee.add(amount.multiply(percentageFee));
    }
}
