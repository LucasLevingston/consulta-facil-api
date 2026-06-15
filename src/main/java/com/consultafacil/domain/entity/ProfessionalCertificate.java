package com.consultafacil.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "professional_certificates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProfessionalCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_profile_id", nullable = false)
    @ToString.Exclude
    private ProfessionalProfile professionalProfile;

    @Column(nullable = false)
    private String title;

    @Column(name = "issuing_organization")
    private String issuingOrganization;

    @Column(name = "issue_year")
    private Integer issueYear;

    @Column(name = "certificate_url")
    private String certificateUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
