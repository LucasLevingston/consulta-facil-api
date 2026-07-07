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
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadDocumentServiceTest {

    @Mock PatientProfileRepositoryPort patientProfileRepository;
    @Mock PatientDocumentRepositoryPort documentRepository;
    @Mock StoragePort storagePort;

    UploadDocumentService service;

    @BeforeEach
    void setUp() {
        service = new UploadDocumentService(
                new PatientProfileFinder(patientProfileRepository), documentRepository, storagePort, new PatientDocumentMapper());
    }

    private PatientProfile profile(String profileId, String userId) {
        User user = User.builder().id(userId).build();
        return PatientProfile.builder().id(profileId).user(user).build();
    }

    @Test
    void uploadDocument_valid_savesToS3AndDb() throws Exception {
        PatientProfile p = profile("pat-1", "user-1");
        MockMultipartFile file = new MockMultipartFile("file", "rg.jpg", "image/jpeg", "content".getBytes());

        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(p));
        when(storagePort.upload(any(), any(), any(), eq("patient-documents")))
                .thenReturn("https://bucket.s3.region.amazonaws.com/patient-documents/uuid.jpg");
        when(documentRepository.save(any())).thenAnswer(inv -> {
            PatientDocument d = inv.getArgument(0);
            return PatientDocument.builder()
                    .id("doc-1").documentType(d.getDocumentType())
                    .fileUrl(d.getFileUrl()).fileName(d.getFileName())
                    .patientProfile(p).build();
        });

        var result = service.execute("user-1", file, DocumentType.RG, null);

        assertThat(result.id()).isEqualTo("doc-1");
        assertThat(result.documentType()).isEqualTo("RG");
        verify(storagePort).upload(any(), eq("rg.jpg"), eq("image/jpeg"), eq("patient-documents"));
    }
}
