package com.consultafacil.api.dto.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class ProfessionalScheduleResponseDTO {

    private String id;
    private String professionalProfileId;
    private String dayOfWeek;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime endTime;

    private Integer consultationDurationMinutes;
    private Integer breakBetweenConsultationsMinutes;
    private Boolean isActive;
}
