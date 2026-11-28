package com.consultafacil.application.service.professional.enrichment;
import com.consultafacil.application.service.professional.profile.ProfessionalChildOwnershipGuard;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileByIdFinder;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileByUserIdFinder;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileMapper;

import com.consultafacil.api.dto.professional.ProfessionalEducationDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProfessionalEducation;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.DegreeType;
import com.consultafacil.domain.port.out.professional.enrichment.ProfessionalEducationRepositoryPort;
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
class UpdateEducationServiceTest {

    @Mock ProfessionalProfileByUserIdFinder profileByUserIdFinder;
    @Mock ProfessionalProfileByIdFinder profileByIdFinder;
    @Mock ProfessionalEducationRepositoryPort educationRepository;
    @Mock ProfessionalChildOwnershipGuard ownershipGuard;
    @Mock ProfessionalProfileMapper mapper;

    @InjectMocks UpdateEducationService service;

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

        ProfessionalEducationDTO dto = new ProfessionalEducationDTO(null, DegreeType.GRADUATION, "USP", null, 2010);
        assertThatThrownBy(() -> service.execute("missing", "edu-1", dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_educationNotFound_throwsResourceNotFoundException() {
        ProfessionalProfile p = profile("prof-1", "user-1");
        when(profileByUserIdFinder.findOrThrow("user-1")).thenReturn(p);
        when(educationRepository.findById("missing")).thenReturn(Optional.empty());

        ProfessionalEducationDTO dto = new ProfessionalEducationDTO(null, DegreeType.GRADUATION, "USP", null, 2010);
        assertThatThrownBy(() -> service.execute("user-1", "missing", dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_validOwner_updatesAllFields() {
        ProfessionalProfile owner = profile("prof-1", "user-1");
        ProfessionalEducation education = ProfessionalEducation.builder()
                .id("edu-1").professionalProfile(owner)
                .degree(DegreeType.GRADUATION).institution("UFMG")
                .fieldOfStudy("Medicine").graduationYear(2010)
                .build();

        when(profileByUserIdFinder.findOrThrow("user-1")).thenReturn(owner);
        when(educationRepository.findById("edu-1")).thenReturn(Optional.of(education));
        when(profileByIdFinder.findOrThrow("prof-1")).thenReturn(owner);
        ProfessionalResponseDTO responseDTO = ProfessionalResponseDTO.builder().id("prof-1").build();
        when(mapper.toResponseDTO(owner)).thenReturn(responseDTO);

        ProfessionalEducationDTO dto = new ProfessionalEducationDTO(null, DegreeType.MASTER, "Unicamp", "Cardiology", 2015);
        service.execute("user-1", "edu-1", dto);

        assertThat(education.getDegree()).isEqualTo(DegreeType.MASTER);
        assertThat(education.getInstitution()).isEqualTo("Unicamp");
        assertThat(education.getFieldOfStudy()).isEqualTo("Cardiology");
        assertThat(education.getGraduationYear()).isEqualTo(2015);
    }

    @Test
    void execute_validOwner_savesEducation() {
        ProfessionalProfile owner = profile("prof-1", "user-1");
        ProfessionalEducation education = ProfessionalEducation.builder()
                .id("edu-1").professionalProfile(owner)
                .degree(DegreeType.GRADUATION).institution("UFMG")
                .build();

        when(profileByUserIdFinder.findOrThrow("user-1")).thenReturn(owner);
        when(educationRepository.findById("edu-1")).thenReturn(Optional.of(education));
        when(profileByIdFinder.findOrThrow("prof-1")).thenReturn(owner);
        when(mapper.toResponseDTO(any())).thenReturn(ProfessionalResponseDTO.builder().id("prof-1").build());

        ProfessionalEducationDTO dto = new ProfessionalEducationDTO(null, DegreeType.MASTER, "Unicamp", null, 2015);
        service.execute("user-1", "edu-1", dto);

        verify(educationRepository).save(education);
    }

    @Test
    void updateEducation_wrongOwner_throwsForbidden() {
        ProfessionalProfile owner = profile("prof-1", "user-1");
        ProfessionalProfile requestor = profile("prof-2", "user-2");
        ProfessionalEducation education = ProfessionalEducation.builder()
                .id("edu-1").professionalProfile(owner)
                .degree(DegreeType.MASTER).institution("Unicamp")
                .build();

        when(profileByUserIdFinder.findOrThrow("user-2")).thenReturn(requestor);
        when(educationRepository.findById("edu-1")).thenReturn(Optional.of(education));
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado"))
                .when(ownershipGuard).assertOwnedBy(owner.getId(), requestor.getId());

        ProfessionalEducationDTO dto = new ProfessionalEducationDTO(null, DegreeType.PHD, "USP", null, 2015);
        assertThatThrownBy(() -> service.execute("user-2", "edu-1", dto))
                .isInstanceOf(ResponseStatusException.class);
    }
}
