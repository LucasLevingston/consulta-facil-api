package com.consultafacil.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "clinic_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ClinicMember {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private ClinicMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("clinicId")
    @JoinColumn(name = "clinic_id")
    @ToString.Exclude
    private Clinic clinic;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("professionalProfileId")
    @JoinColumn(name = "professional_profile_id")
    @ToString.Exclude
    private ProfessionalProfile professionalProfile;

    @Column(nullable = false)
    @Builder.Default
    private String role = "MEMBER";

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();
}
