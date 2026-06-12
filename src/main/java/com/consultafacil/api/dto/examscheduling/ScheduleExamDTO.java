package com.consultafacil.api.dto.examscheduling;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ScheduleExamDTO {

    @NotBlank
    private String examRequestId;

    @NotBlank
    private String examLabId;

    @NotNull
    private LocalDate scheduledDate;

    @NotNull
    private LocalTime scheduledTime;

    private String notes;
}
