package com.example.consulta.domain.entity;

import com.example.consulta.domain.enums.AppointmentModality;
import com.example.consulta.domain.enums.AppointmentPaymentStatus;
import com.example.consulta.domain.enums.AppointmentStatus;
import com.example.consulta.domain.enums.PaymentMethod;
import com.example.consulta.domain.exception.InvalidStateException;
import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments", indexes = {
    @Index(name = "idx_patient_id", columnList = "patient_id"),
    @Index(name = "idx_professional_id", columnList = "professional_id"),
    @Index(name = "idx_scheduled_at", columnList = "scheduled_at"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private String id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    @ToString.Exclude
    private PatientProfile patient;

    @ManyToOne
    @JoinColumn(name = "professional_id", nullable = false)
    @ToString.Exclude
    private ProfessionalProfile professional;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AppointmentModality modality = AppointmentModality.IN_PERSON;

    @Column(length = 255)
    private String meetLink;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String cancellationReason;

    @Column
    private LocalDateTime previousScheduledAt;

    @Column
    private LocalDateTime checkedInAt;

    @Column
    private LocalDateTime calledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AppointmentPaymentStatus paymentStatus = AppointmentPaymentStatus.UNPAID;

    @Column(precision = 10, scale = 2)
    private BigDecimal paymentAmount;

    @ManyToOne
    @JoinColumn(name = "service_id")
    @ToString.Exclude
    private ProfessionalService service;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private PaymentMethod chosenPaymentMethod;

    @Column(length = 255)
    private String paymentPreferenceId;

    @Column(length = 255)
    private String paymentId;

    @Column
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String ratingComment;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // --- Factory method ---

    public static Appointment schedule(PatientProfile patient, ProfessionalProfile professional,
                                       LocalDateTime scheduledAt, String reason, String notes,
                                       AppointmentModality modality, ProfessionalService service,
                                       BigDecimal paymentAmount, PaymentMethod paymentMethod) {
        return Appointment.builder()
                .patient(patient)
                .professional(professional)
                .scheduledAt(scheduledAt)
                .reason(reason)
                .notes(notes)
                .modality(modality != null ? modality : AppointmentModality.IN_PERSON)
                .status(AppointmentStatus.PENDING)
                .service(service)
                .paymentAmount(paymentAmount)
                .chosenPaymentMethod(paymentMethod)
                .build();
    }

    // --- Domain behaviour methods ---

    public void confirm() {
        if (this.status != AppointmentStatus.PENDING) {
            throw new InvalidStateException(
                    "Only PENDING appointments can be confirmed. Current: " + status);
        }
        this.status = AppointmentStatus.CONFIRMED;
    }

    public void cancel(String reason) {
        if (this.status == AppointmentStatus.COMPLETED
                || this.status == AppointmentStatus.CANCELED) {
            throw new InvalidStateException(
                    "Cannot cancel appointment with status: " + status);
        }
        this.status = AppointmentStatus.CANCELED;
        this.cancellationReason = reason;
    }

    public void complete() {
        if (this.status != AppointmentStatus.CONFIRMED
                && this.status != AppointmentStatus.IN_PROGRESS) {
            throw new InvalidStateException(
                    "Only CONFIRMED or IN_PROGRESS appointments can be completed. Current: " + status);
        }
        this.status = AppointmentStatus.COMPLETED;
    }

    public void checkIn() {
        if (this.status != AppointmentStatus.CONFIRMED
                && this.status != AppointmentStatus.PENDING) {
            throw new InvalidStateException(
                    "Appointment is not in a check-in eligible status. Current: " + status);
        }
        this.status = AppointmentStatus.CHECKED_IN;
        this.checkedInAt = LocalDateTime.now();
    }

    public void callNext() {
        if (this.status != AppointmentStatus.CHECKED_IN) {
            throw new InvalidStateException(
                    "Patient has not checked in yet. Current: " + status);
        }
        this.status = AppointmentStatus.IN_PROGRESS;
        this.calledAt = LocalDateTime.now();
    }

    public void reschedule(LocalDateTime newScheduledAt, String newReason) {
        if (this.status != AppointmentStatus.PENDING
                && this.status != AppointmentStatus.CONFIRMED) {
            throw new InvalidStateException(
                    "Only PENDING or CONFIRMED appointments can be rescheduled. Current: " + status);
        }
        this.previousScheduledAt = this.scheduledAt;
        this.scheduledAt = newScheduledAt;
        if (newReason != null) {
            this.reason = newReason;
        }
    }

    public void rate(int stars, String comment) {
        if (this.status != AppointmentStatus.COMPLETED) {
            throw new InvalidStateException("Only COMPLETED appointments can be rated");
        }
        if (this.rating != null) {
            throw new InvalidStateException("Appointment has already been rated");
        }
        this.rating = stars;
        this.ratingComment = comment;
    }
}
