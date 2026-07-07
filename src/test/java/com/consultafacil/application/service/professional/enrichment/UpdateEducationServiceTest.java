package com.consultafacil.application.service.professional.enrichment;
import com.consultafacil.application.service.professional.profile.ProfessionalChildOwnershipGuard;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileByIdFinder;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileByUserIdFinder;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileMapper;

import com.consultafacil.api.dto.professional.ProfessionalEducationDTO;
import com.consultafacil.domain.entity.ProfessionalEducation;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.DegreeType;
import com.consultafacil.domain.port.out.ProfessionalEducationRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
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
                .build();
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
