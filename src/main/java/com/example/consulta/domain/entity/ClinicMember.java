package com.example.consulta.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "clinic_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicMember {

    @EmbeddedId
    private ClinicMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("clinicId")
    @JoinColumn(name = "clinic_id")
    private Clinic clinic;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("doctorProfileId")
    @JoinColumn(name = "doctor_profile_id")
    private DoctorProfile doctorProfile;

    @Column(nullable = false)
    @Builder.Default
    private String role = "MEMBER";

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();
}
