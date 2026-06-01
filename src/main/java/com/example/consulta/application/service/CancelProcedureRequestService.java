package com.example.consulta.application.service;

import com.example.consulta.api.dto.procedurerequest.ProcedureRequestResponseDTO;
import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.ProcedureRequest;
import com.example.consulta.domain.enums.ProcedureRequestStatus;
import com.example.consulta.domain.repository.PatientProfileRepository;
import com.example.consulta.domain.repository.ProcedureRequestRepository;
import com.example.consulta.domain.repository.ProfessionalProfileRepository;
import com.example.consulta.application.port.in.CancelProcedureRequestUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelProcedureRequestService implements CancelProcedureRequestUseCase {

    private final ProcedureRequestRepository procedureRequestRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;

    @Transactional
    public ProcedureRequestResponseDTO execute(String requestId, String userId) {
        ProcedureRequest request = procedureRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("ProcedureRequest", requestId));

        boolean isPatient = patientProfileRepository.findByUserId(userId)
                .map(p -> p.getId().equals(request.getPatient().getId()))
                .orElse(false);

        boolean isProfessional = professionalProfileRepository.findByUserId(userId)
                .map(p -> p.getId().equals(request.getProfessional().getId()))
                .orElse(false);

        if (!isPatient && !isProfessional) {
            throw new BadRequestException("You are not authorized to cancel this procedure request");
        }

        if (request.getStatus() == ProcedureRequestStatus.COMPLETED
                || request.getStatus() == ProcedureRequestStatus.CANCELED) {
            throw new BadRequestException("Cannot cancel a request with status: " + request.getStatus());
        }

        request.setStatus(ProcedureRequestStatus.CANCELED);
        return CreateProcedureRequestService.toResponseDTO(procedureRequestRepository.save(request));
    }
}
