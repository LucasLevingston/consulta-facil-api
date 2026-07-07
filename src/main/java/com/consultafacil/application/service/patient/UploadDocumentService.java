package com.consultafacil.application.service.patient;

import com.consultafacil.api.dto.patient.PatientDocumentResponseDTO;
import com.consultafacil.application.port.in.patient.UploadDocumentUseCase;
import com.consultafacil.domain.entity.PatientDocument;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.enums.DocumentType;
import com.consultafacil.domain.port.out.patient.PatientDocumentRepositoryPort;
import com.consultafacil.domain.port.out.StoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UploadDocumentService implements UploadDocumentUseCase {

    private final PatientProfileFinder profileFinder;
    private final PatientDocumentRepositoryPort documentRepository;
    private final StoragePort storagePort;
    private final PatientDocumentMapper mapper;

    @Override
    @Transactional
    public PatientDocumentResponseDTO execute(String userId, MultipartFile file,
                                               DocumentType documentType, String documentLabel) {
        PatientProfile profile = profileFinder.findOrThrow(userId);
        String fileUrl;
        try {
            fileUrl = storagePort.upload(file.getBytes(), file.getOriginalFilename(),
                    file.getContentType(), "patient-documents");
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload document", e);
        }
        PatientDocument doc = PatientDocument.builder()
                .patientProfile(profile)
                .documentType(documentType)
                .documentLabel(documentLabel)
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .build();
        return mapper.toDTO(documentRepository.save(doc));
    }
}
