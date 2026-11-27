package com.consultafacil.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "health_insurance_plans", indexes = {
    @Index(name = "idx_insurance_plan_code", columnList = "code"),
    @Index(name = "idx_insurance_plan_active", columnList = "active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthInsurancePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20, unique = true)
    private String code;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
