package com.consultafacil.application.service.professional.enrichment;
import com.consultafacil.application.service.professional.profile.ProfessionalChildOwnershipGuard;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileByIdFinder;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileByUserIdFinder;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileMapper;

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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteCertificateServiceTest {

    @Mock ProfessionalProfileByUserIdFinder profileByUserIdFinder;
    @Mock ProfessionalProfileByIdFinder profileByIdFinder;
    @Mock ProfessionalCertificateRepositoryPort certificateRepository;
    @Mock ProfessionalChildOwnershipGuard ownershipGuard;
    @Mock ProfessionalProfileMapper mapper;

    @InjectMocks DeleteCertificateService service;

    private ProfessionalProfile profile(String profileId, String userId) {
        User user = User.builder().id(userId).build();
        return ProfessionalProfile.builder()
                .id(profileId).user(user)
                .education(new ArrayList<>())
                .experience(new ArrayList<>())
                .certificates(new ArrayList<>())
                .clinicMemberships(new ArrayList<>())
                .build();
    }

    @Test
    void execute_profileNotFound_throwsResourceNotFoundException() {
        when(profileByUserIdFinder.findOrThrow("missing"))
                .thenThrow(new ResourceNotFoundException("Professional profile", "missing"));

        assertThatThrownBy(() -> service.execute("missing", "cert-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_certificateNotFound_throwsResourceNotFoundException() {
        ProfessionalProfile p = profile("prof-1", "user-1");
        when(profileByUserIdFinder.findOrThrow("user-1")).thenReturn(p);
        when(certificateRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("user-1", "missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_validOwner_deletesCertificate() {
        ProfessionalProfile owner = profile("prof-1", "user-1");
        ProfessionalCertificate cert = ProfessionalCertificate.builder()
                .id("cert-1").professionalProfile(owner).title("ACLS")
                .build();

        when(profileByUserIdFinder.findOrThrow("user-1")).thenReturn(owner);
        when(certificateRepository.findById("cert-1")).thenReturn(Optional.of(cert));
        when(profileByIdFinder.findOrThrow("prof-1")).thenReturn(owner);
        when(mapper.toResponseDTO(owner)).thenReturn(ProfessionalResponseDTO.builder().id("prof-1").build());

        service.execute("user-1", "cert-1");

        verify(certificateRepository).delete(cert);
    }

    @Test
    void execute_validOwner_returnsUpdatedProfile() {
        ProfessionalProfile owner = profile("prof-1", "user-1");
        ProfessionalCertificate cert = ProfessionalCertificate.builder()
                .id("cert-1").professionalProfile(owner).title("BLS")
                .build();
        ProfessionalResponseDTO expected = ProfessionalResponseDTO.builder().id("prof-1").build();

        when(profileByUserIdFinder.findOrThrow("user-1")).thenReturn(owner);
        when(certificateRepository.findById("cert-1")).thenReturn(Optional.of(cert));
        when(profileByIdFinder.findOrThrow("prof-1")).thenReturn(owner);
        when(mapper.toResponseDTO(owner)).thenReturn(expected);

        ProfessionalResponseDTO result = service.execute("user-1", "cert-1");

        assertThat(result).isSameAs(expected);
    }

    @Test
    void deleteCertificate_wrongOwner_throwsForbidden() {
        ProfessionalProfile owner = profile("prof-1", "user-1");
        ProfessionalProfile requestor = profile("prof-2", "user-2");
        ProfessionalCertificate cert = ProfessionalCertificate.builder()
                .id("cert-1").professionalProfile(owner).title("ACLS")
                .build();

        when(profileByUserIdFinder.findOrThrow("user-2")).thenReturn(requestor);
        when(certificateRepository.findById("cert-1")).thenReturn(Optional.of(cert));
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado"))
                .when(ownershipGuard).assertOwnedBy(owner.getId(), requestor.getId());

        assertThatThrownBy(() -> service.execute("user-2", "cert-1"))
                .isInstanceOf(ResponseStatusException.class);
    }
}
