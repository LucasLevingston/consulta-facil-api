package com.consultafacil.domain.entity;

import com.consultafacil.domain.enums.FinancialAuditAction;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false, length = 36)
    private String entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FinancialAuditAction action;

    @Column(name = "actor_id", length = 36)
    private String actorId;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Builder.Default
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private LocalDateTime occurredAt = LocalDateTime.now();
}
