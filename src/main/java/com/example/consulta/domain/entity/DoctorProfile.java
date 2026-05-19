package com.example.consulta.domain.entity;

import com.example.consulta.domain.enums.DoctorProfileStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctor_profiles", indexes = {
    @Index(name = "idx_specialty", columnList = "specialty")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String specialty;

    @Column(nullable = false, unique = true)
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DoctorProfileStatus status = DoctorProfileStatus.PENDING_REVIEW;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();
}
