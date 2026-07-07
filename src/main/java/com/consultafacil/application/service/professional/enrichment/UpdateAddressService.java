package com.consultafacil.application.service.professional.enrichment;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileByUserIdFinder;
import com.consultafacil.application.service.professional.profile.ProfessionalProfileMapper;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.professional.UpdateAddressDTO;
import com.consultafacil.application.port.in.UpdateAddressUseCase;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateAddressService implements UpdateAddressUseCase {

    private final ProfessionalProfileByUserIdFinder profileFinder;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final ProfessionalProfileMapper mapper;

    @Override
    @Transactional
    public ProfessionalResponseDTO execute(String userId, UpdateAddressDTO dto) {
        ProfessionalProfile profile = profileFinder.findOrThrow(userId);
        if (dto.city() != null) profile.setCity(dto.city());
        if (dto.state() != null) profile.setState(dto.state());
        if (dto.address() != null) profile.setAddress(dto.address());
        if (dto.zipCode() != null) profile.setZipCode(dto.zipCode());
        if (dto.neighborhood() != null) profile.setNeighborhood(dto.neighborhood());
        if (dto.streetNumber() != null) profile.setStreetNumber(dto.streetNumber());
        if (dto.complement() != null) profile.setComplement(dto.complement());
        if (dto.latitude() != null) profile.setLatitude(dto.latitude());
        if (dto.longitude() != null) profile.setLongitude(dto.longitude());
        return mapper.toResponseDTO(professionalProfileRepository.save(profile));
    }
}
