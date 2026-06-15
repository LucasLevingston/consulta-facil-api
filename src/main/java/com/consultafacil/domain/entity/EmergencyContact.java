package com.consultafacil.domain.entity;

import com.consultafacil.domain.enums.EmergencyContactRelationship;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "emergency_contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EmergencyContact {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    private String email;

    @Enumerated(EnumType.STRING)
    private EmergencyContactRelationship relationship;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_profile_id", nullable = false)
    @ToString.Exclude
    private PatientProfile patientProfile;
}
