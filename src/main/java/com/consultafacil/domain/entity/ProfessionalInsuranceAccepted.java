package com.consultafacil.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "professional_insurance_accepted")
@IdClass(ProfessionalInsurancePK.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalInsuranceAccepted {

    @Id
    @Column(name = "professional_id")
    private String professionalId;

    @Id
    @Column(name = "plan_id")
    private String planId;

    @Column(length = 300)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", insertable = false, updatable = false)
    @ToString.Exclude
    private HealthInsurancePlan plan;
}
