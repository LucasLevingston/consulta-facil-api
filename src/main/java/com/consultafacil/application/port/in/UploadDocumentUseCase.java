package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.patient.PatientDocumentResponseDTO;
import com.consultafacil.domain.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

public interface UploadDocumentUseCase {

    PatientDocumentResponseDTO execute(String userId, MultipartFile file, DocumentType documentType, String documentLabel);
}
