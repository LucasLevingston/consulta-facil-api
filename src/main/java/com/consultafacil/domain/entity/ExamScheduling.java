package com.consultafacil.domain.entity;

import com.consultafacil.domain.enums.ExamSchedulingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "exam_schedulings", indexes = {
    @Index(name = "idx_exam_schedulings_request", columnList = "exam_request_id"),
    @Index(name = "idx_exam_schedulings_lab_date", columnList = "exam_lab_id, scheduled_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ExamScheduling {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_request_id", nullable = false)
    @ToString.Exclude
    private ExamRequest examRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_lab_id", nullable = false)
    @ToString.Exclude
    private ExamLab examLab;

    @Column(nullable = false)
    private LocalDate scheduledDate;

    @Column(nullable = false)
    private LocalTime scheduledTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ExamSchedulingStatus status = ExamSchedulingStatus.SCHEDULED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
