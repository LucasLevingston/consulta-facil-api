package com.consultafacil.api.dto.professionalservice;

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
public class ProfessionalServiceResponseDTO {
    private String id;
    private String professionalId;
    private String professionalName;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationMinutes;
    private boolean requiresConsultation;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
