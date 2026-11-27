package com.consultafacil.api.dto.prescription;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PrescriptionDTO(
        String id,
        String appointmentId,
        String professionalName,
        String patientName,
        String medications,
        String instructions,
        LocalDate validUntil,
        String pdfUrl,
        LocalDateTime createdAt
) {}
