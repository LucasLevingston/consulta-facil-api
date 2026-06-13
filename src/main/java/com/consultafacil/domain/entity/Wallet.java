package com.consultafacil.domain.entity;

import com.consultafacil.core.exception.BadRequestException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "pending_balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal pendingBalance = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new BadRequestException("Saldo insuficiente");
        }
        this.balance = this.balance.subtract(amount);
    }

    public void addPending(BigDecimal amount) {
        this.pendingBalance = this.pendingBalance.add(amount);
    }

    public void releasePending(BigDecimal amount) {
        this.pendingBalance = this.pendingBalance.subtract(amount).max(BigDecimal.ZERO);
    }
}
