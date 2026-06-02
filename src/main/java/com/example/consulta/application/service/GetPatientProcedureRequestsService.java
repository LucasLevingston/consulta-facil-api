package com.example.consulta.application.service;

import com.example.consulta.api.dto.procedurerequest.ProcedureRequestResponseDTO;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.port.out.PatientProfileRepositoryPort;
import com.example.consulta.domain.port.out.ProcedureRequestRepositoryPort;
import com.example.consulta.application.port.in.GetPatientProcedureRequestsUseCase;
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
