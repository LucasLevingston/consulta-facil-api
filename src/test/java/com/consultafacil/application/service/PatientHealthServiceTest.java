package com.consultafacil.application.service;

import com.consultafacil.api.dto.patient.EmergencyContactDTO;
import com.consultafacil.api.dto.patient.PatientVaccineDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.EmergencyContact;
import com.consultafacil.domain.entity.PatientDocument;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.PatientVaccine;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.DocumentType;
import com.consultafacil.domain.enums.EmergencyContactRelationship;
import com.consultafacil.domain.port.out.EmergencyContactRepositoryPort;
import com.consultafacil.domain.port.out.PatientDocumentRepositoryPort;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.PatientVaccineRepositoryPort;
import com.consultafacil.domain.port.out.StoragePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientHealthServiceTest {

    @Mock PatientProfileRepositoryPort patientProfileRepository;
    @Mock EmergencyContactRepositoryPort emergencyContactRepository;
    @Mock PatientVaccineRepositoryPort vaccineRepository;
    @Mock PatientDocumentRepositoryPort documentRepository;
    @Mock StoragePort storagePort;

    @InjectMocks PatientHealthService service;

    private PatientProfile profile(String profileId, String userId) {
        User user = User.builder().id(userId).build();
        return PatientProfile.builder().id(profileId).user(user).build();
    }

    // ── Emergency Contacts ────────────────────────────────────────────────

    @Test
    void addEmergencyContact_valid_savesAndReturns() {
        PatientProfile p = profile("pat-1", "user-1");
        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(p));
        when(emergencyContactRepository.save(any())).thenAnswer(inv -> {
            EmergencyContact c = inv.getArgument(0);
            return EmergencyContact.builder()
                    .id("ec-1").name(c.getName()).phone(c.getPhone())
                    .email(c.getEmail()).relationship(c.getRelationship())
                    .patientProfile(p).build();
        });

        EmergencyContactDTO dto = new EmergencyContactDTO(null, "Maria", "11999999999",
                "maria@email.com", EmergencyContactRelationship.MOTHER);
        EmergencyContactDTO result = service.addEmergencyContact("user-1", dto);

        assertThat(result.id()).isEqualTo("ec-1");
        assertThat(result.name()).isEqualTo("Maria");
        assertThat(result.relationship()).isEqualTo(EmergencyContactRelationship.MOTHER);
    }

    @Test
    void addEmergencyContact_profileNotFound_throwsNotFound() {
        when(patientProfileRepository.findByUserId("unknown")).thenReturn(Optional.empty());
        EmergencyContactDTO dto = new EmergencyContactDTO(null, "João", "11888888888", null, null);
        assertThatThrownBy(() -> service.addEmergencyContact("unknown", dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateEmergencyContact_wrongOwner_throwsForbidden() {
        PatientProfile owner = profile("pat-1", "user-1");
        PatientProfile requestor = profile("pat-2", "user-2");
        EmergencyContact contact = EmergencyContact.builder()
                .id("ec-1").name("Carlos").phone("11777777777").patientProfile(owner).build();

        when(patientProfileRepository.findByUserId("user-2")).thenReturn(Optional.of(requestor));
        when(emergencyContactRepository.findById("ec-1")).thenReturn(Optional.of(contact));

        EmergencyContactDTO dto = new EmergencyContactDTO(null, "Carlos Updated", "11777777777", null, null);
        assertThatThrownBy(() -> service.updateEmergencyContact("user-2", "ec-1", dto))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void deleteEmergencyContact_valid_deletesEntry() {
        PatientProfile p = profile("pat-1", "user-1");
        EmergencyContact contact = EmergencyContact.builder()
                .id("ec-1").name("Ana").phone("11666666666").patientProfile(p).build();

        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(p));
        when(emergencyContactRepository.findById("ec-1")).thenReturn(Optional.of(contact));

        service.deleteEmergencyContact("user-1", "ec-1");

        verify(emergencyContactRepository).delete(contact);
    }

    @Test
    void listEmergencyContacts_returnsAll() {
        PatientProfile p = profile("pat-1", "user-1");
        EmergencyContact c1 = EmergencyContact.builder().id("ec-1").name("A").phone("111").patientProfile(p).build();
        EmergencyContact c2 = EmergencyContact.builder().id("ec-2").name("B").phone("222").patientProfile(p).build();

        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(p));
        when(emergencyContactRepository.findByPatientProfileId("pat-1")).thenReturn(List.of(c1, c2));

        List<EmergencyContactDTO> result = service.listEmergencyContacts("user-1");

        assertThat(result).hasSize(2);
    }

    // ── Vaccines ──────────────────────────────────────────────────────────

    @Test
    void addVaccine_valid_savesAndReturns() {
        PatientProfile p = profile("pat-1", "user-1");
        when(patientProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(p));
        when(vaccineRepository.save(any())).thenAnswer(inv -> {
            PatientVaccine v = inv.getArgument(0);
            return PatientVaccine.builder()
                    .id("vac-1").vaccineName(v.getVaccineName())
                    .doseNumber(v.getDoseNumber()).patientProfile(p).build();
        });

        PatientVaccineDTO dto = new PatientVaccineDTO(null, "COVID-19", "2ª dose", null, null);
        PatientVaccineDTO result = service.addVaccine("user-1", dto);

        assertThat(result.id()).isEqualTo("vac-1");
        assertThat(result.vaccineName()).isEqualTo("COVID-19");
    }

    @Test
    void deleteVaccine_wrongOwner_throwsForbidden() {
        PatientProfile owner = profile("pat-1", "user-1");
        PatientProfile requestor = profile("pat-2", "user-2");
        PatientVaccine vaccine = PatientVaccine.builder()
                .id("vac-1").vaccineName("Flu").patientProfile(owner).build();

        when(patientProfileRepository.findByUserId("user-2")).thenReturn(Optional.of(requestor));
        when(vaccineRepository.findById("vac-1")).thenReturn(Optional.of(vaccine));

        assertThatThrownBy(() -> service.deleteVaccine("user-2", "vac-1"))
                .isInstanceOf(ResponseStatusException.class);
    }

    // ── Documents ─────────────────────────────────────────────────────────

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

        var result = service.uploadDocument("user-1", file, DocumentType.RG, null);

        assertThat(result.id()).isEqualTo("doc-1");
        assertThat(result.documentType()).isEqualTo("RG");
        verify(storagePort).upload(any(), eq("rg.jpg"), eq("image/jpeg"), eq("patient-documents"));
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

        service.deleteDocument("user-1", "doc-1");

        verify(storagePort).delete("patient-documents/uuid.pdf");
        verify(documentRepository).delete(doc);
    }
}
