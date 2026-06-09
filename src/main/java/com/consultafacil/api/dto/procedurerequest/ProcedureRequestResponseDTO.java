package com.consultafacil.api.dto.procedurerequest;

import com.consultafacil.domain.enums.ProcedureRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcedureRequestResponseDTO {
    private String id;
    private String serviceId;
    private String serviceName;
    private BigDecimal servicePrice;
    private Integer serviceDurationMinutes;
    private String patientId;
    private String patientName;
    private String professionalId;
    private String professionalName;
    private String appointmentId;
    private String notes;
    private ProcedureRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
