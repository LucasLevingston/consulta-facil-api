package com.example.consulta.application.service;

import com.example.consulta.api.dto.procedurerequest.ProcedureRequestResponseDTO;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.repository.ProcedureRequestRepository;
import com.example.consulta.domain.repository.ProfessionalProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetProfessionalProcedureRequestsService {

    private final ProcedureRequestRepository procedureRequestRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;

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
