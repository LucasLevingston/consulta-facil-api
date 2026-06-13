package com.consultafacil.domain.entity;

import com.consultafacil.domain.enums.PaymentMethod;
import com.consultafacil.domain.enums.PaymentTiming;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import com.consultafacil.domain.enums.ProfessionalType;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.exception.InvalidStateException;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Enumerated(EnumType.STRING)
    private ProfessionalType profession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Specialty specialty;

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

    @ElementCollection(targetClass = PaymentMethod.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "professional_payment_methods", joinColumns = @JoinColumn(name = "professional_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    @BatchSize(size = 20)
    @Builder.Default
    private Set<PaymentMethod> acceptedPaymentMethods = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentTiming paymentTiming = PaymentTiming.AT_CONSULTATION;

    @OneToMany(mappedBy = "professional", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    @Builder.Default
    @ToString.Exclude
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "professionalProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    @Builder.Default
    @ToString.Exclude
    private List<ClinicMember> clinicMemberships = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "instagram_url")
    private String instagramUrl;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "website_url")
    private String websiteUrl;


    // --- Domain behaviour methods ---

    public void approve() {
        if (this.status != ProfessionalProfileStatus.PENDING_REVIEW) {
            throw new InvalidStateException(
                    "Application is not pending review. Current: " + status);
        }
        this.status = ProfessionalProfileStatus.ACTIVE;
    }

    public void reject() {
        if (this.status != ProfessionalProfileStatus.PENDING_REVIEW) {
            throw new InvalidStateException(
                    "Application is not pending review. Current: " + status);
        }
        this.status = ProfessionalProfileStatus.REJECTED;
    }

    public void updateConsultationPrice(java.math.BigDecimal price) {
        this.consultationPrice = price;
    }

    public void setPaymentConfiguration(Set<PaymentMethod> methods, PaymentTiming timing) {
        this.acceptedPaymentMethods = methods != null ? methods : new HashSet<>();
        if (timing != null) this.paymentTiming = timing;
    }
}
