package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.procedurerequest.ProcedureRequestResponseDTO;

import java.util.List;

public interface GetProfessionalProcedureRequestsUseCase {

    List<ProcedureRequestResponseDTO> execute(String professionalUserId);
}
