package com.example.consulta.api.dto.appointment;

import com.example.consulta.domain.enums.AppointmentStatus;
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
    private String doctorName;
    private String doctorId;
    private String specialty;
    private LocalDateTime scheduledAt;
    private String reason;
    private String notes;
    private AppointmentStatus status;
    private String cancellationReason;
    private Integer rating;
    private String ratingComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
