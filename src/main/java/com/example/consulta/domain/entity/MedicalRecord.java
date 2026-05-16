package com.example.consulta.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "medical_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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

    @OneToOne
    @JoinColumn(name = "patient_profile_id", nullable = false, unique = true)
    private PatientProfile patientProfile;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
