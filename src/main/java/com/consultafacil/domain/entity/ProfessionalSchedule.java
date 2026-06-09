package com.consultafacil.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "professional_schedules", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"professional_profile_id", "day_of_week"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProfessionalSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_profile_id", nullable = false)
    @ToString.Exclude
    private ProfessionalProfile professional;

    @Column(nullable = false)
    private String dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    @Builder.Default
    private Integer consultationDurationMinutes = 30;

    @Column(nullable = false)
    @Builder.Default
    private Integer breakBetweenConsultationsMinutes = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
