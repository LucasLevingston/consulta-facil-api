package com.consultafacil.application.service;

import com.consultafacil.api.dto.patient.PatientDocumentResponseDTO;
import com.consultafacil.domain.entity.PatientDocument;
import org.springframework.stereotype.Component;

@Component
public class PatientDocumentMapper {

    public PatientDocumentResponseDTO toDTO(PatientDocument d) {
        return new PatientDocumentResponseDTO(
                d.getId(),
                d.getDocumentType() != null ? d.getDocumentType().name() : null,
                d.getDocumentLabel(),
                d.getFileUrl(),
                d.getFileName(),
                d.getUploadedAt() != null ? d.getUploadedAt().toString() : null
        );
    }
}
