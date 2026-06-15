package com.consultafacil.api.dto.patient;

public record PatientDocumentResponseDTO(
        String id,
        String documentType,
        String documentLabel,
        String fileUrl,
        String fileName,
        String uploadedAt
) {}
