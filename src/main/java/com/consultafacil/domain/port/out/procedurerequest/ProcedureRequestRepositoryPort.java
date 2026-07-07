package com.consultafacil.domain.port.out.procedurerequest;

import com.consultafacil.domain.entity.ProcedureRequest;

import java.util.List;
import java.util.Optional;

public interface ProcedureRequestRepositoryPort {

    ProcedureRequest save(ProcedureRequest request);

    Optional<ProcedureRequest> findById(String id);

    List<ProcedureRequest> findByPatientId(String patientId);

    List<ProcedureRequest> findByProfessionalId(String professionalId);
}
