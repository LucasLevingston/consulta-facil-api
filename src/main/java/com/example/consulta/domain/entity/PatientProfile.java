package com.example.consulta.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "patient_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String occupation;

    @OneToOne(mappedBy = "patientProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private EmergencyContact emergencyContact;

    @OneToOne(mappedBy = "patientProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private MedicalRecord medicalRecord;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();
}
