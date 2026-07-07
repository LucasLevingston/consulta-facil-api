package com.consultafacil.application.service.professional.enrichment;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileByUserIdFinder;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileMapper;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.professional.UpdateAddressDTO;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateAddressServiceTest {

    @Mock ProfessionalProfileByUserIdFinder profileFinder;
    @Mock ProfessionalProfileRepositoryPort professionalProfileRepository;
    @Mock ProfessionalProfileMapper mapper;

    @InjectMocks UpdateAddressService service;

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
    void updateAddress_validUser_setsAddressFields() {
        ProfessionalProfile p = profile("prof-1", "user-1");
        when(profileFinder.findOrThrow("user-1")).thenReturn(p);
        when(professionalProfileRepository.save(p)).thenReturn(p);
        ProfessionalResponseDTO dto = ProfessionalResponseDTO.builder().id(p.getId()).build();
        when(mapper.toResponseDTO(p)).thenReturn(dto);

        UpdateAddressDTO input = new UpdateAddressDTO("São Paulo", "SP", "Av. Paulista", "01310-100", "Bela Vista", "1000", null, -23.5, -46.6);
        service.execute("user-1", input);

        assertThat(p.getCity()).isEqualTo("São Paulo");
        assertThat(p.getZipCode()).isEqualTo("01310-100");
        assertThat(p.getLatitude()).isEqualTo(-23.5);
    }
}
