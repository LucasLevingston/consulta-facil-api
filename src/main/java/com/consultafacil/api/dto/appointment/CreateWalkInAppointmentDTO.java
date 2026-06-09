package com.consultafacil.api.dto.appointment;

import com.consultafacil.domain.enums.AppointmentPaymentStatus;
import com.consultafacil.domain.enums.WalkInPaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateWalkInAppointmentDTO {

    // Patient — either patientId or (patientName + patientCpf)
    private String patientId;
    private String patientName;
    private String patientCpf;
    private String patientPhone;

    @NotNull(message = "professionalId é obrigatório")
    private String professionalId;

    private String clinicId;

    @NotNull(message = "performedAt é obrigatório")
    private LocalDateTime performedAt;

    private Integer durationMinutes;
    private String reason;
    private String notes;

    @NotNull(message = "paymentStatus é obrigatório")
    private AppointmentPaymentStatus paymentStatus;

    private BigDecimal paymentAmount;
    private WalkInPaymentMethod paymentMethod;

    private WalkInClinicalNoteDTO clinicalNote;
}
