package com.consultafacil.api.dto.examscheduling;

import com.consultafacil.domain.enums.ExamSchedulingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class ExamSchedulingResponseDTO {

    private String id;
    private String examRequestId;
    private String examName;
    private String examLabId;
    private String examLabName;
    private String examLabAddress;
    private String examLabCity;
    private String examLabPhone;
    private LocalDate scheduledDate;
    private LocalTime scheduledTime;
    private ExamSchedulingStatus status;
    private String notes;
    private LocalDateTime createdAt;
}
