package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.patient.EmergencyContactDTO;
import com.consultafacil.api.dto.patient.PatientDocumentResponseDTO;
import com.consultafacil.api.dto.patient.PatientVaccineDTO;
import com.consultafacil.domain.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PatientHealthUseCase {

    List<EmergencyContactDTO> listEmergencyContacts(String userId);

    EmergencyContactDTO addEmergencyContact(String userId, EmergencyContactDTO dto);

    EmergencyContactDTO updateEmergencyContact(String userId, String contactId, EmergencyContactDTO dto);

    void deleteEmergencyContact(String userId, String contactId);

    List<PatientVaccineDTO> listVaccines(String userId);

    PatientVaccineDTO addVaccine(String userId, PatientVaccineDTO dto);

    void deleteVaccine(String userId, String vaccineId);

    List<PatientDocumentResponseDTO> listDocuments(String userId);

    PatientDocumentResponseDTO uploadDocument(String userId, MultipartFile file, DocumentType documentType, String documentLabel);

    void deleteDocument(String userId, String documentId);
}
