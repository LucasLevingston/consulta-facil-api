package com.example.consulta.application.service;

import com.example.consulta.api.dto.procedurerequest.ProcedureRequestResponseDTO;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.port.out.ProcedureRequestRepositoryPort;
import com.example.consulta.domain.port.out.ProfessionalProfileRepositoryPort;
import com.example.consulta.application.port.in.GetProfessionalProcedureRequestsUseCase;
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
