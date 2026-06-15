package com.consultafacil.domain.entity;

import com.consultafacil.domain.enums.BloodType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "medical_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MedicalRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Column(columnDefinition = "TEXT")
    private String currentMedication;

    @Column(columnDefinition = "TEXT")
    private String familyMedicalHistory;

    @Column(columnDefinition = "TEXT")
    private String pastMedicalHistory;

    @Builder.Default
    private Boolean privacyConsent = false;

    @Builder.Default
    private Boolean treatmentConsent = false;

    @Builder.Default
    private Boolean disclosureConsent = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_type")
    private BloodType bloodType;

    @Column(precision = 5, scale = 2)
    private BigDecimal height;

    @Column(precision = 5, scale = 2)
    private BigDecimal weight;

    @OneToOne
    @JoinColumn(name = "patient_profile_id", nullable = false, unique = true)
    @ToString.Exclude
    private PatientProfile patientProfile;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
