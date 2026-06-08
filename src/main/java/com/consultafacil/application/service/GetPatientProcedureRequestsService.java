package com.consultafacil.application.service;

import com.consultafacil.api.dto.procedurerequest.ProcedureRequestResponseDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.port.out.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.ProcedureRequestRepositoryPort;
import com.consultafacil.application.port.in.GetPatientProcedureRequestsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPatientProcedureRequestsService implements GetPatientProcedureRequestsUseCase {

    private final ProcedureRequestRepositoryPort procedureRequestRepository;
    private final PatientProfileRepositoryPort patientProfileRepository;

    @Transactional(readOnly = true)
    public List<ProcedureRequestResponseDTO> execute(String patientUserId) {
        String patientProfileId = patientProfileRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("PatientProfile not found for user: " + patientUserId))
                .getId();
        return procedureRequestRepository.findByPatientId(patientProfileId)
                .stream()
                .map(CreateProcedureRequestService::toResponseDTO)
                .toList();
    }
}
