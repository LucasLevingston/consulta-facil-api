package com.consultafacil.application.port.in.procedurerequest;

import com.consultafacil.api.dto.procedurerequest.ProcedureRequestResponseDTO;

public interface CancelProcedureRequestUseCase {

    ProcedureRequestResponseDTO execute(String requestId, String userId);
}
