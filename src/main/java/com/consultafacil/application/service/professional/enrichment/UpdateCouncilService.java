package com.consultafacil.application.service.professional.enrichment;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileByUserIdFinder;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileMapper;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.professional.UpdateCouncilDTO;
import com.consultafacil.application.port.in.UpdateCouncilUseCase;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateCouncilService implements UpdateCouncilUseCase {

    private final ProfessionalProfileByUserIdFinder profileFinder;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final ProfessionalProfileMapper mapper;

    @Override
    @Transactional
    public ProfessionalResponseDTO execute(String userId, UpdateCouncilDTO dto) {
        ProfessionalProfile profile = profileFinder.findOrThrow(userId);
        profile.setCouncilType(dto.councilType());
        profile.setCouncilState(dto.councilState());
        return mapper.toResponseDTO(professionalProfileRepository.save(profile));
    }
}
