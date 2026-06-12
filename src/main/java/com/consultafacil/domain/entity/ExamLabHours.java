package com.consultafacil.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "exam_lab_hours", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"exam_lab_id", "day_of_week"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ExamLabHours {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_lab_id", nullable = false)
    @ToString.Exclude
    private ExamLab examLab;

    @Column(nullable = false)
    private String dayOfWeek;

    @Column(nullable = false)
    private LocalTime openTime;

    @Column(nullable = false)
    private LocalTime closeTime;

    @Column(nullable = false)
    @Builder.Default
    private Integer slotDurationMinutes = 30;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isOpen = true;
}
