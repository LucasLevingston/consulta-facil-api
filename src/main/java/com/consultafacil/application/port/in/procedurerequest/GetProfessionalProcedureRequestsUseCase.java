package com.consultafacil.application.port.in.procedurerequest;

import com.consultafacil.api.dto.procedurerequest.ProcedureRequestResponseDTO;

import java.util.List;

public interface GetProfessionalProcedureRequestsUseCase {

    List<ProcedureRequestResponseDTO> execute(String professionalUserId);
}
