package com.consultafacil.application.service.procedurerequest;

import com.consultafacil.api.dto.procedurerequest.ProcedureRequestResponseDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.port.out.procedurerequest.ProcedureRequestRepositoryPort;
import com.consultafacil.domain.port.out.professional.profile.ProfessionalProfileRepositoryPort;
import com.consultafacil.application.port.in.procedurerequest.GetProfessionalProcedureRequestsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetProfessionalProcedureRequestsService implements GetProfessionalProcedureRequestsUseCase {

    private final ProcedureRequestRepositoryPort procedureRequestRepository;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;

    @Transactional(readOnly = true)
    public List<ProcedureRequestResponseDTO> execute(String professionalUserId) {
        String professionalProfileId = professionalProfileRepository.findByUserId(professionalUserId)
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile not found for user: " + professionalUserId))
                .getId();
        return procedureRequestRepository.findByProfessionalId(professionalProfileId)
                .stream()
                .map(CreateProcedureRequestService::toResponseDTO)
                .toList();
    }
}
