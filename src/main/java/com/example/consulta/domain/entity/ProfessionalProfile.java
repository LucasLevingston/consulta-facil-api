package com.example.consulta.domain.entity;

import com.example.consulta.domain.enums.ProfessionalProfileStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "professional_profiles", indexes = {
    @Index(name = "idx_specialty", columnList = "specialty")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProfessionalProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @ToString.Exclude
    private User user;

    private String profession;

    @Column(nullable = false)
    private String specialty;

    @Column(nullable = false, unique = true)
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProfessionalProfileStatus status = ProfessionalProfileStatus.PENDING_REVIEW;

    private String city;
    private String state;
    private String address;

    private Double latitude;

    private Double longitude;

    @Column(precision = 10, scale = 2)
    private BigDecimal consultationPrice;

    @OneToMany(mappedBy = "professional", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "professionalProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<ClinicMember> clinicMemberships = new ArrayList<>();
}
