package com.consultafacil.api.dto.examlab;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class ExamLabHoursDTO {

    @NotBlank
    private String dayOfWeek;

    @NotNull
    private LocalTime openTime;

    @NotNull
    private LocalTime closeTime;

    private Integer slotDurationMinutes = 30;
    private Boolean isOpen = true;
}
