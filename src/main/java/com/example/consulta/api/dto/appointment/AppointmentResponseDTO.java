package com.example.consulta.api.dto.appointment;

import com.example.consulta.domain.enums.AppointmentModality;
import com.example.consulta.domain.enums.AppointmentPaymentStatus;
import com.example.consulta.domain.enums.AppointmentStatus;
import com.example.consulta.domain.enums.PaymentMethod;
import com.example.consulta.domain.enums.PaymentTiming;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponseDTO {
    private String id;
    private String patientName;
    private String patientId;
    private String professionalName;
    private String professionalId;
    private String specialty;
    private LocalDateTime scheduledAt;
    private LocalDateTime previousScheduledAt;
    private LocalDateTime checkedInAt;
    private LocalDateTime calledAt;
    private String reason;
    private String notes;
    private AppointmentModality modality;
    private String meetLink;
    private AppointmentStatus status;
    private String cancellationReason;
    private AppointmentPaymentStatus paymentStatus;
    private BigDecimal paymentAmount;
    private PaymentMethod chosenPaymentMethod;
    private PaymentTiming paymentTiming;
    private String checkoutUrl;
    private Integer rating;
    private String ratingComment;
    private String serviceId;
    private String serviceName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
