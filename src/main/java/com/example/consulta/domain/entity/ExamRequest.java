package com.example.consulta.domain.entity;

import com.example.consulta.domain.enums.ExamRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "exam_requests", indexes = {
    @Index(name = "idx_exam_requests_appointment_id", columnList = "appointment_id"),
    @Index(name = "idx_exam_requests_patient_id", columnList = "patient_id"),
    @Index(name = "idx_exam_requests_professional_id", columnList = "professional_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ExamRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    @ManyToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    @ToString.Exclude
    private Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "professional_id", nullable = false)
    @ToString.Exclude
    private ProfessionalProfile professional;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    @ToString.Exclude
    private PatientProfile patient;

    @Column(nullable = false)
    private String examName;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ExamRequestStatus status = ExamRequestStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String fileUrl;

    @Column
    private String fileName;

    @Column(columnDefinition = "TEXT")
    private String professionalNotes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
