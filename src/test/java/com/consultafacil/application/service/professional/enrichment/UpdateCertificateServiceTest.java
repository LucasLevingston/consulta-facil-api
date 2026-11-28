package com.consultafacil.application.service.professional.enrichment;

import com.consultafacil.application.service.professional.profile.ProfessionalChildOwnershipGuard;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileByIdFinder;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileByUserIdFinder;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileMapper;

import com.consultafacil.api.dto.professional.ProfessionalCertificateDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalCertificate;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.professional.enrichment.ProfessionalCertificateRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCertificateServiceTest {

    @Mock ProfessionalProfileByUserIdFinder profileByUserIdFinder;
    @Mock ProfessionalProfileByIdFinder profileByIdFinder;
    @Mock ProfessionalCertificateRepositoryPort certificateRepository;
    @Mock ProfessionalChildOwnershipGuard ownershipGuard;
    @Mock ProfessionalProfileMapper mapper;

    @InjectMocks UpdateCertificateService service;

    private ProfessionalProfile profile(String profileId, String userId) {
        User user = User.builder().id(userId).build();
        return ProfessionalProfile.builder()
                .id(profileId).user(user)
                .education(new ArrayList<>())
                .experience(new ArrayList<>())
                .certificates(new ArrayList<>())
                .build();
    }

    @Test
    void updateCertificate_valid_savesCertificate() {
        ProfessionalProfile p = profile("prof-1", "user-1");
        ProfessionalCertificate cert = ProfessionalCertificate.builder()
                .id("cert-1").professionalProfile(p)
                .title("ACLS").issuingOrganization("AHA").issueYear(2021)
                .build();

        when(profileByUserIdFinder.findOrThrow("user-1")).thenReturn(p);
        when(certificateRepository.findById("cert-1")).thenReturn(Optional.of(cert));
        when(certificateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(profileByIdFinder.findOrThrow("prof-1")).thenReturn(p);
        ProfessionalResponseDTO dto = ProfessionalResponseDTO.builder().id(p.getId()).build();
        when(mapper.toResponseDTO(p)).thenReturn(dto);

        ProfessionalCertificateDTO input = new ProfessionalCertificateDTO(null, "BLS", "AHA", 2022, null);
        service.execute("user-1", "cert-1", input);

        assertThat(cert.getTitle()).isEqualTo("BLS");
        assertThat(cert.getIssueYear()).isEqualTo(2022);
        verify(certificateRepository).save(cert);
    }

    @Test
    void updateCertificate_certificateNotFound_throwsNotFound() {
        ProfessionalProfile p = profile("prof-1", "user-1");
        when(profileByUserIdFinder.findOrThrow("user-1")).thenReturn(p);
        when(certificateRepository.findById("missing")).thenReturn(Optional.empty());

        ProfessionalCertificateDTO input = new ProfessionalCertificateDTO(null, "BLS", "AHA", 2022, null);
        assertThatThrownBy(() -> service.execute("user-1", "missing", input))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCertificate_wrongOwner_throwsForbidden() {
        ProfessionalProfile owner = profile("prof-1", "user-1");
        ProfessionalProfile requestor = profile("prof-2", "user-2");
        ProfessionalCertificate cert = ProfessionalCertificate.builder()
                .id("cert-1").professionalProfile(owner)
                .title("ACLS").issuingOrganization("AHA").issueYear(2021)
                .build();

        when(profileByUserIdFinder.findOrThrow("user-2")).thenReturn(requestor);
        when(certificateRepository.findById("cert-1")).thenReturn(Optional.of(cert));
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado"))
                .when(ownershipGuard).assertOwnedBy(owner.getId(), requestor.getId());

        ProfessionalCertificateDTO input = new ProfessionalCertificateDTO(null, "BLS", "AHA", 2022, null);
        assertThatThrownBy(() -> service.execute("user-2", "cert-1", input))
                .isInstanceOf(ResponseStatusException.class);
    }
}
