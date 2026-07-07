package com.consultafacil.application.service;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.application.port.in.SearchProfessionalsBySpecialtyUseCase;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import com.consultafacil.domain.enums.Specialty;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchProfessionalsBySpecialtyService implements SearchProfessionalsBySpecialtyUseCase {

    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final ProfessionalProfileMapper mapper;

    @Override
    public Page<ProfessionalResponseDTO> execute(String specialty, Pageable pageable) {
        try {
            Specialty specialtyEnum = Specialty.valueOf(specialty.toUpperCase());
            return professionalProfileRepository
                    .findBySpecialtyAndStatus(specialtyEnum, ProfessionalProfileStatus.ACTIVE, pageable)
                    .map(mapper::toResponseDTO);
        } catch (IllegalArgumentException e) {
            return Page.empty(pageable);
        }
    }
}
