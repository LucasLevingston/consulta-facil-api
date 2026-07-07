package com.consultafacil.application.service;

import com.consultafacil.domain.entity.PatientDocument;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.DocumentType;
import com.consultafacil.domain.port.out.PatientDocumentRepositoryPort;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.StoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteDocumentServiceTest {

    @Mock PatientProfileRepositoryPort patientProfileRepository;
    @Mock PatientDocumentRepositoryPort documentRepository;
    @Mock StoragePort storagePort;

    DeleteDocumentService service;

    @BeforeEach
    void setUp() {
        service = new DeleteDocumentService(
                new PatientProfileFinder(patientProfileRepository), new PatientOwnershipGuard(), documentRepository, storagePort);
    }

    private PatientProfile profile(String profileId, String userId) {
        User user = User.builder().id(userId).build();
        return PatientProfile.builder().id(profileId).user(user).build();
    }

    @Test
    void deleteDocument_valid_removesFromS3AndDb() {
        PatientProfile p = profile("pat-1", "user-1");
        PatientDocument doc = PatientDocument.builder()
                .id("doc-1").documentType(DocumentType.CPF)
                .fileUrl("https://bucket.s3.us-east-1.amazonaws.com/patient-documents/uuid.pdf")
                .patientProfile(p).build();

        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(p));
        when(documentRepository.findById("doc-1")).thenReturn(Optional.of(doc));

        service.execute("user-1", "doc-1");

        verify(storagePort).delete("patient-documents/uuid.pdf");
        verify(documentRepository).delete(doc);
    }
}
