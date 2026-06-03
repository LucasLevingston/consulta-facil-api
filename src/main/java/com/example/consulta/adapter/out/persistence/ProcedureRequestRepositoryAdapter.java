package com.example.consulta.adapter.out.persistence;

import com.example.consulta.domain.entity.ProcedureRequest;
import com.example.consulta.domain.port.out.ProcedureRequestRepositoryPort;
import com.example.consulta.domain.repository.ProcedureRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProcedureRequestRepositoryAdapter implements ProcedureRequestRepositoryPort {

    private final ProcedureRequestRepository procedureRequestRepository;

    @Override
    public ProcedureRequest save(ProcedureRequest request) {
        return procedureRequestRepository.save(request);
    }

    @Override
    public Optional<ProcedureRequest> findById(String id) {
        return procedureRequestRepository.findById(id);
    }

    @Override
    public List<ProcedureRequest> findByPatientId(String patientId) {
        return procedureRequestRepository.findByPatientId(patientId);
    }

    @Override
    public List<ProcedureRequest> findByProfessionalId(String professionalId) {
        return procedureRequestRepository.findByProfessionalId(professionalId);
    }
}
