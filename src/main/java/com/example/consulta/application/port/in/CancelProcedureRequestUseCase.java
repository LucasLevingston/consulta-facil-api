package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.procedurerequest.ProcedureRequestResponseDTO;

public interface CancelProcedureRequestUseCase {

    ProcedureRequestResponseDTO execute(String requestId, String userId);
}
