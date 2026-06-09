package com.consultafacil.api.dto.exam;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateExamRequestDTO {

    @NotBlank(message = "Exam name is required")
    private String examName;

    private String instructions;
}
