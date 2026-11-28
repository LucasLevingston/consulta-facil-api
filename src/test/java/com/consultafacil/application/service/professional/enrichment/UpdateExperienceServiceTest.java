package com.consultafacil.application.service.professional.enrichment;

import com.consultafacil.application.service.professional.profile.ProfessionalChildOwnershipGuard;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileByIdFinder;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileByUserIdFinder;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileMapper;

import com.consultafacil.api.dto.professional.ProfessionalExperienceDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalExperience;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.professional.enrichment.ProfessionalExperienceRepositoryPort;
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
class UpdateExperienceServiceTest {

    @Mock ProfessionalProfileByUserIdFinder profileByUserIdFinder;
    @Mock ProfessionalProfileByIdFinder profileByIdFinder;
    @Mock ProfessionalExperienceRepositoryPort experienceRepository;
    @Mock ProfessionalChildOwnershipGuard ownershipGuard;
    @Mock ProfessionalProfileMapper mapper;

    @InjectMocks UpdateExperienceService service;

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
    void updateExperience_valid_savesExperience() {
        ProfessionalProfile p = profile("prof-1", "user-1");
        ProfessionalExperience experience = ProfessionalExperience.builder()
                .id("exp-1").professionalProfile(p)
                .position("Cardiologista").institution("Hospital das Clinicas")
                .startYear(2015).endYear(2020)
                .build();

        when(profileByUserIdFinder.findOrThrow("user-1")).thenReturn(p);
        when(experienceRepository.findById("exp-1")).thenReturn(Optional.of(experience));
        when(experienceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(profileByIdFinder.findOrThrow("prof-1")).thenReturn(p);
        ProfessionalResponseDTO dto = ProfessionalResponseDTO.builder().id(p.getId()).build();
        when(mapper.toResponseDTO(p)).thenReturn(dto);

        ProfessionalExperienceDTO input = new ProfessionalExperienceDTO(null, "Neurologista", "UNIFESP", 2016, 2021, "Residencia");
        service.execute("user-1", "exp-1", input);

        assertThat(experience.getPosition()).isEqualTo("Neurologista");
        assertThat(experience.getInstitution()).isEqualTo("UNIFESP");
        assertThat(experience.getStartYear()).isEqualTo(2016);
        assertThat(experience.getEndYear()).isEqualTo(2021);
        assertThat(experience.getDescription()).isEqualTo("Residencia");
        verify(experienceRepository).save(experience);
    }

    @Test
    void updateExperience_experienceNotFound_throwsNotFound() {
        ProfessionalProfile p = profile("prof-1", "user-1");
        when(profileByUserIdFinder.findOrThrow("user-1")).thenReturn(p);
        when(experienceRepository.findById("missing")).thenReturn(Optional.empty());

        ProfessionalExperienceDTO input = new ProfessionalExperienceDTO(null, "Neurologista", "UNIFESP", 2016, null, null);
        assertThatThrownBy(() -> service.execute("user-1", "missing", input))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateExperience_wrongOwner_throwsForbidden() {
        ProfessionalProfile owner = profile("prof-1", "user-1");
        ProfessionalProfile requestor = profile("prof-2", "user-2");
        ProfessionalExperience experience = ProfessionalExperience.builder()
                .id("exp-1").professionalProfile(owner)
                .position("Cardiologista").institution("Hospital das Clinicas")
                .startYear(2015)
                .build();

        when(profileByUserIdFinder.findOrThrow("user-2")).thenReturn(requestor);
        when(experienceRepository.findById("exp-1")).thenReturn(Optional.of(experience));
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado"))
                .when(ownershipGuard).assertOwnedBy(owner.getId(), requestor.getId());

        ProfessionalExperienceDTO input = new ProfessionalExperienceDTO(null, "Neurologista", "UNIFESP", 2016, null, null);
        assertThatThrownBy(() -> service.execute("user-2", "exp-1", input))
                .isInstanceOf(ResponseStatusException.class);
    }
}
