package com.consultafacil.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "patient_insurance", indexes = {
    @Index(name = "idx_patient_insurance_patient", columnList = "patient_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientInsurance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "patient_id", nullable = false)
    private String patientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    @ToString.Exclude
    private HealthInsurancePlan plan;

    @Column(name = "member_number", nullable = false, length = 50)
    private String memberNumber;

    @Column(name = "holder_name", nullable = false, length = 100)
    private String holderName;

    @Column(name = "valid_until")
    private LocalDate validUntil;
}
