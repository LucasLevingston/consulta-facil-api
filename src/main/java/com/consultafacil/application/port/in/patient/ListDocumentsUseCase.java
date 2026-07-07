package com.consultafacil.application.port.in.patient;

import com.consultafacil.api.dto.patient.PatientDocumentResponseDTO;

import java.util.List;

public interface ListDocumentsUseCase {

    List<PatientDocumentResponseDTO> execute(String userId);
}
