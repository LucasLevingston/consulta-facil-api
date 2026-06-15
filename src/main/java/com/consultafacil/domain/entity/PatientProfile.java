package com.consultafacil.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "patient_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PatientProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @ToString.Exclude
    private User user;

    private String occupation;

    @OneToMany(mappedBy = "patientProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @BatchSize(size = 20)
    @ToString.Exclude
    private List<EmergencyContact> emergencyContacts = new ArrayList<>();

    @OneToOne(mappedBy = "patientProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private MedicalRecord medicalRecord;

    @OneToMany(mappedBy = "patientProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @BatchSize(size = 20)
    @ToString.Exclude
    private List<PatientVaccine> vaccines = new ArrayList<>();

    @OneToMany(mappedBy = "patientProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @BatchSize(size = 20)
    @ToString.Exclude
    private List<PatientDocument> documents = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Appointment> appointments = new ArrayList<>();
}
