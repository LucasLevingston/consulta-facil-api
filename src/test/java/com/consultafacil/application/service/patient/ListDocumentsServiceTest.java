package com.consultafacil.application.service.patient;

import com.consultafacil.api.dto.patient.PatientDocumentResponseDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.PatientDocument;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.DocumentType;
import com.consultafacil.domain.port.out.patient.PatientDocumentRepositoryPort;
import com.consultafacil.domain.port.out.patient.PatientProfileRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListDocumentsServiceTest {

    @Mock PatientProfileRepositoryPort patientProfileRepository;
    @Mock PatientDocumentRepositoryPort documentRepository;

    ListDocumentsService service;

    PatientProfile profile;

    @BeforeEach
    void setUp() {
        User user = User.builder().id("user-1").name("Carlos").email("carlos@email.com").build();
        profile = PatientProfile.builder().id("pat-1").user(user).build();

        service = new ListDocumentsService(
                new PatientProfileFinder(patientProfileRepository),
                documentRepository,
                new PatientDocumentMapper());
    }

    @Test
    void execute_profileNotFound_throwsResourceNotFoundException() {
        when(patientProfileRepository.findByUserId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_noDocuments_returnsEmptyList() {
        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(profile));
        when(documentRepository.findByPatientProfileId("pat-1")).thenReturn(List.of());

        List<PatientDocumentResponseDTO> result = service.execute("user-1");

        assertThat(result).isEmpty();
    }

    @Test
    void execute_withDocuments_returnsMappedList() {
        PatientDocument doc1 = PatientDocument.builder()
                .id("doc-1")
                .patientProfile(profile)
                .documentType(DocumentType.CPF)
                .documentLabel("CPF document")
                .fileUrl("https://example.com/doc1.pdf")
                .fileName("cpf.pdf")
                .build();
        PatientDocument doc2 = PatientDocument.builder()
                .id("doc-2")
                .patientProfile(profile)
                .documentType(DocumentType.RG)
                .documentLabel("RG document")
                .fileUrl("https://example.com/doc2.pdf")
                .fileName("rg.pdf")
                .build();

        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(profile));
        when(documentRepository.findByPatientProfileId("pat-1")).thenReturn(List.of(doc1, doc2));

        List<PatientDocumentResponseDTO> result = service.execute("user-1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo("doc-1");
        assertThat(result.get(0).documentType()).isEqualTo("CPF");
        assertThat(result.get(1).id()).isEqualTo("doc-2");
        assertThat(result.get(1).documentType()).isEqualTo("RG");
    }

    @Test
    void execute_documentMappedWithCorrectFileUrl() {
        PatientDocument doc = PatientDocument.builder()
                .id("doc-3")
                .patientProfile(profile)
                .documentType(DocumentType.CNH)
                .fileUrl("https://bucket.s3.amazonaws.com/cnh.jpg")
                .fileName("cnh.jpg")
                .build();

        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(profile));
        when(documentRepository.findByPatientProfileId("pat-1")).thenReturn(List.of(doc));

        List<PatientDocumentResponseDTO> result = service.execute("user-1");

        assertThat(result.get(0).fileUrl()).isEqualTo("https://bucket.s3.amazonaws.com/cnh.jpg");
        assertThat(result.get(0).fileName()).isEqualTo("cnh.jpg");
    }
}
