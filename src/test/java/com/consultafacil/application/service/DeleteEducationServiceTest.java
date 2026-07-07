package com.consultafacil.application.service;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
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

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteEducationServiceTest {

    @Mock ProfessionalProfileByUserIdFinder profileByUserIdFinder;
    @Mock ProfessionalProfileByIdFinder profileByIdFinder;
    @Mock ProfessionalEducationRepositoryPort educationRepository;
    @Mock ProfessionalChildOwnershipGuard ownershipGuard;
    @Mock ProfessionalProfileMapper mapper;

    @InjectMocks DeleteEducationService service;

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
    void deleteEducation_valid_deletesEntry() {
        ProfessionalProfile p = profile("prof-1", "user-1");
        ProfessionalEducation education = ProfessionalEducation.builder()
                .id("edu-1").professionalProfile(p)
                .degree(DegreeType.GRADUATION).institution("UFRJ")
                .build();

        when(profileByUserIdFinder.findOrThrow("user-1")).thenReturn(p);
        when(educationRepository.findById("edu-1")).thenReturn(Optional.of(education));
        when(profileByIdFinder.findOrThrow("prof-1")).thenReturn(p);
        ProfessionalResponseDTO dto = ProfessionalResponseDTO.builder().id(p.getId()).build();
        when(mapper.toResponseDTO(p)).thenReturn(dto);

        service.execute("user-1", "edu-1");

        verify(educationRepository).delete(education);
    }
}
