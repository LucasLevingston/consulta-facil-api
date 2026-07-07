package com.consultafacil.application.service.clinic;

import com.consultafacil.api.dto.clinic.ClinicResponseDTO;
import com.consultafacil.api.dto.clinic.CreateClinicDTO;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Clinic;
import com.consultafacil.domain.port.out.clinic.ClinicRepositoryPort;
import com.consultafacil.application.port.in.clinic.UpdateClinicUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateClinicService implements UpdateClinicUseCase {

    private final ClinicRepositoryPort clinicRepository;
    private final ClinicMapper clinicMapper;

    @Transactional
    public ClinicResponseDTO execute(String clinicId, String userId, CreateClinicDTO dto) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic", clinicId));

        if (!clinic.getOwner().getId().equals(userId)) {
            throw new BadRequestException("You are not the owner of this clinic");
        }

        clinic.setName(dto.getName());
        clinic.setDescription(dto.getDescription());
        clinic.setPhone(dto.getPhone());
        clinic.setAddress(dto.getAddress());
        clinic.setCity(dto.getCity());
        clinic.setState(dto.getState());
        clinic.setZipCode(dto.getZipCode());
        clinic.setLatitude(dto.getLatitude());
        clinic.setLongitude(dto.getLongitude());
        if (dto.getImageUrl() != null) clinic.setImageUrl(dto.getImageUrl());

        return clinicMapper.toResponseDTO(clinicRepository.save(clinic));
    }
}
