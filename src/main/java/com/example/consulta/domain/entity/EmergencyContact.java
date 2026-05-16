package com.example.consulta.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "emergency_contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyContact {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @OneToOne
    @JoinColumn(name = "patient_profile_id", nullable = false, unique = true)
    private PatientProfile patientProfile;
}
