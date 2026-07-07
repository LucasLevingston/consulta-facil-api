package com.consultafacil.application.service.professional.profile;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.application.port.in.GetPendingApplicationsUseCase;
import com.consultafacil.domain.enums.ProfessionalProfileStatus;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPendingApplicationsService implements GetPendingApplicationsUseCase {

    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final ProfessionalProfileMapper mapper;

    @Override
    public Page<ProfessionalResponseDTO> execute(Pageable pageable) {
        return professionalProfileRepository.findByStatus(ProfessionalProfileStatus.PENDING_REVIEW, pageable)
                .map(mapper::toResponseDTO);
    }
}
