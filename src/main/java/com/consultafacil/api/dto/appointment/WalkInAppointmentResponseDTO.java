package com.consultafacil.api.dto.appointment;

import com.consultafacil.domain.enums.AppointmentPaymentStatus;
import com.consultafacil.domain.enums.AppointmentSource;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.enums.WalkInPaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class WalkInAppointmentResponseDTO {
    private String id;
    private String patientId;
    private String patientName;
    private String professionalId;
    private String professionalName;
    private LocalDateTime performedAt;
    private Integer durationMinutes;
    private String reason;
    private String notes;
    private AppointmentStatus status;
    private AppointmentSource source;
    private AppointmentPaymentStatus paymentStatus;
    private BigDecimal paymentAmount;
    private WalkInPaymentMethod paymentMethod;
    private String clinicalNoteId;
    private LocalDateTime createdAt;
}
