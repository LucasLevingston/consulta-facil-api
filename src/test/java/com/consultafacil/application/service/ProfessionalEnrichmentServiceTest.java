package com.consultafacil.application.service;

import com.consultafacil.api.dto.professional.ProfessionalCertificateDTO;
import com.consultafacil.api.dto.professional.ProfessionalEducationDTO;
import com.consultafacil.api.dto.professional.ProfessionalExperienceDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.professional.UpdateAddressDTO;
import com.consultafacil.api.dto.professional.UpdateCouncilDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalCertificate;
import com.consultafacil.domain.entity.ProfessionalEducation;
import com.consultafacil.domain.entity.ProfessionalExperience;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.CouncilType;
import com.consultafacil.domain.enums.DegreeType;
import com.consultafacil.domain.port.out.ProfessionalCertificateRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalEducationRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalExperienceRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfessionalEnrichmentServiceTest {

    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;
    @Mock ProfessionalEducationRepositoryPort educationRepository;
    @Mock ProfessionalExperienceRepositoryPort experienceRepository;
    @Mock ProfessionalCertificateRepositoryPort certificateRepository;
    @Mock ProfessionalService professionalService;

    @InjectMocks ProfessionalEnrichmentService service;

    private ProfessionalProfile profile(String profileId, String userId) {
        User user = User.builder().id(userId).build();
        return ProfessionalProfile.builder()
                .id(profileId).user(user)
                .education(new ArrayList<>())
                .experience(new ArrayList<>())
                .certificates(new ArrayList<>())
                .build();
    }

    private ProfessionalResponseDTO stubResponse(ProfessionalProfile p) {
        ProfessionalResponseDTO dto = ProfessionalResponseDTO.builder().id(p.getId()).build();
        when(professionalService.toResponseDTO(p)).thenReturn(dto);
        return dto;
    }

    @Test
    void updateCouncil_validUser_setsCouncilFields() {
        ProfessionalProfile p = profile("prof-1", "user-1");
        when(professionalProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(p));
        when(professionalProfileRepository.save(p)).thenReturn(p);
        stubResponse(p);

        UpdateCouncilDTO dto = new UpdateCouncilDTO(CouncilType.CRM, "SP");
        ProfessionalResponseDTO result = service.updateCouncil("user-1", dto);

        assertThat(p.getCouncilType()).isEqualTo(CouncilType.CRM);
        assertThat(p.getCouncilState()).isEqualTo("SP");
        assertThat(result.getId()).isEqualTo("prof-1");
    }

    @Test
    void updateAddress_validUser_setsAddressFields() {
        ProfessionalProfile p = profile("prof-1", "user-1");
        when(professionalProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(p));
        when(professionalProfileRepository.save(p)).thenReturn(p);
        stubResponse(p);

        UpdateAddressDTO dto = new UpdateAddressDTO("São Paulo", "SP", "Av. Paulista", "01310-100", "Bela Vista", "1000", null, -23.5, -46.6);
        service.updateAddress("user-1", dto);

        assertThat(p.getCity()).isEqualTo("São Paulo");
        assertThat(p.getZipCode()).isEqualTo("01310-100");
        assertThat(p.getLatitude()).isEqualTo(-23.5);
    }

    @Test
    void updateCouncil_profileNotFound_throwsNotFound() {
        when(professionalProfileRepository.findByUserId("unknown")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateCouncil("unknown", new UpdateCouncilDTO(CouncilType.CRM, "RJ")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addEducation_valid_savesAndReturnsProfile() {
        ProfessionalProfile p = profile("prof-1", "user-1");
        when(professionalProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(p));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(p));
        when(educationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        stubResponse(p);

        ProfessionalEducationDTO dto = new ProfessionalEducationDTO(null, DegreeType.GRADUATION, "USP", "Medicina", 2010);
        ProfessionalResponseDTO result = service.addEducation("user-1", dto);

        verify(educationRepository).save(any(ProfessionalEducation.class));
        assertThat(result.getId()).isEqualTo("prof-1");
    }

    @Test
    void updateEducation_wrongOwner_throwsForbidden() {
        ProfessionalProfile owner = profile("prof-1", "user-1");
        ProfessionalProfile requestor = profile("prof-2", "user-2");
        ProfessionalEducation education = ProfessionalEducation.builder()
                .id("edu-1").professionalProfile(owner)
                .degree(DegreeType.MASTER).institution("Unicamp")
                .build();

        when(professionalProfileRepository.findByUserId("user-2")).thenReturn(Optional.of(requestor));
        when(educationRepository.findById("edu-1")).thenReturn(Optional.of(education));

        ProfessionalEducationDTO dto = new ProfessionalEducationDTO(null, DegreeType.PHD, "USP", null, 2015);
        assertThatThrownBy(() -> service.updateEducation("user-2", "edu-1", dto))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void deleteEducation_valid_deletesEntry() {
        ProfessionalProfile p = profile("prof-1", "user-1");
        ProfessionalEducation education = ProfessionalEducation.builder()
                .id("edu-1").professionalProfile(p)
                .degree(DegreeType.GRADUATION).institution("UFRJ")
                .build();

        when(professionalProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(p));
        when(educationRepository.findById("edu-1")).thenReturn(Optional.of(education));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(p));
        stubResponse(p);

        service.deleteEducation("user-1", "edu-1");

        verify(educationRepository).delete(education);
    }

    @Test
    void addExperience_valid_savesEntry() {
        ProfessionalProfile p = profile("prof-1", "user-1");
        when(professionalProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(p));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(p));
        when(experienceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        stubResponse(p);

        ProfessionalExperienceDTO dto = new ProfessionalExperienceDTO(null, "Cardiologista", "Hospital das Clínicas", 2015, 2020, null);
        service.addExperience("user-1", dto);

        verify(experienceRepository).save(any(ProfessionalExperience.class));
    }

    @Test
    void addCertificate_valid_savesEntry() {
        ProfessionalProfile p = profile("prof-1", "user-1");
        when(professionalProfileRepository.findByUserId("user-1")).thenReturn(Optional.of(p));
        when(professionalProfileRepository.findById("prof-1")).thenReturn(Optional.of(p));
        when(certificateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        stubResponse(p);

        ProfessionalCertificateDTO dto = new ProfessionalCertificateDTO(null, "ACLS", "AHA", 2021, null);
        service.addCertificate("user-1", dto);

        verify(certificateRepository).save(any(ProfessionalCertificate.class));
    }

    @Test
    void deleteCertificate_wrongOwner_throwsForbidden() {
        ProfessionalProfile owner = profile("prof-1", "user-1");
        ProfessionalProfile requestor = profile("prof-2", "user-2");
        ProfessionalCertificate cert = ProfessionalCertificate.builder()
                .id("cert-1").professionalProfile(owner).title("ACLS")
                .build();

        when(professionalProfileRepository.findByUserId("user-2")).thenReturn(Optional.of(requestor));
        when(certificateRepository.findById("cert-1")).thenReturn(Optional.of(cert));

        assertThatThrownBy(() -> service.deleteCertificate("user-2", "cert-1"))
                .isInstanceOf(ResponseStatusException.class);
    }
}
