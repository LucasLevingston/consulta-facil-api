package com.consultafacil.api.dto.exam;

import com.consultafacil.domain.enums.ExamRequestStatus;
import com.consultafacil.domain.enums.ExamType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamRequestResponseDTO {
    private String id;
    private String appointmentId;
    private String professionalId;
    private String professionalName;
    private String patientId;
    private String patientName;
    private ExamType examName;
    private String instructions;
    private ExamRequestStatus status;
    private String fileUrl;
    private String fileName;
    private String professionalNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
