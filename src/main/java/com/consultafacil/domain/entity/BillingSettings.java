package com.consultafacil.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "billing_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingSettings {

    @Id
    private String id;

    @Column(name = "default_currency", nullable = false, length = 3)
    @Builder.Default
    private String defaultCurrency = "BRL";

    @Column(name = "default_gateway", nullable = false, length = 30)
    @Builder.Default
    private String defaultGateway = "MOCK";

    @Column(name = "webhook_secret", length = 200)
    private String webhookSecret;

    @Column(name = "pix_expiration_minutes", nullable = false)
    @Builder.Default
    private int pixExpirationMinutes = 30;

    @Column(name = "invoice_expiration_days", nullable = false)
    @Builder.Default
    private int invoiceExpirationDays = 7;

    @Column(name = "default_trial_days", nullable = false)
    @Builder.Default
    private int defaultTrialDays = 14;

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
}
