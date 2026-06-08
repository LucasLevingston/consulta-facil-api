package com.consultafacil.api.dto.exam;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewExamRequestDTO {

    @NotBlank(message = "Professional notes are required")
    private String professionalNotes;
}
