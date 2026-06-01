package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.procedurerequest.CreateProcedureRequestDTO;
import com.example.consulta.api.dto.procedurerequest.ProcedureRequestResponseDTO;

public interface CreateProcedureRequestUseCase {

    ProcedureRequestResponseDTO execute(String professionalUserId, CreateProcedureRequestDTO dto);
}
