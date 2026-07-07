package com.consultafacil.application.service.procedurerequest;

import com.consultafacil.api.dto.procedurerequest.ProcedureRequestResponseDTO;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.ProcedureRequest;
import com.consultafacil.domain.enums.ProcedureRequestStatus;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.ProcedureRequestRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import com.consultafacil.application.port.in.CancelProcedureRequestUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelProcedureRequestService implements CancelProcedureRequestUseCase {

    private final ProcedureRequestRepositoryPort procedureRequestRepository;
    private final PatientProfileRepositoryPort patientProfileRepository;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;

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
