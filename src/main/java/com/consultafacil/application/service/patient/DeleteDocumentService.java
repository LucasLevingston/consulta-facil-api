package com.consultafacil.application.service.patient;

import com.consultafacil.application.port.in.DeleteDocumentUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.PatientDocument;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.port.out.PatientDocumentRepositoryPort;
import com.consultafacil.domain.port.out.StoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteDocumentService implements DeleteDocumentUseCase {

    private final PatientProfileFinder profileFinder;
    private final PatientOwnershipGuard ownershipGuard;
    private final PatientDocumentRepositoryPort documentRepository;
    private final StoragePort storagePort;

    @Override
    @Transactional
    public void execute(String userId, String documentId) {
        PatientProfile profile = profileFinder.findOrThrow(userId);
        PatientDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("PatientDocument", documentId));
        ownershipGuard.assertOwnership(doc.getPatientProfile().getId(), profile.getId());
        String key = doc.getFileUrl().split("amazonaws.com/", 2)[1];
        storagePort.delete(key);
        documentRepository.delete(doc);
    }
}
