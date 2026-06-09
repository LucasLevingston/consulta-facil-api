package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.procedurerequest.CreateProcedureRequestDTO;
import com.consultafacil.api.dto.procedurerequest.ProcedureRequestResponseDTO;

public interface CreateProcedureRequestUseCase {

    ProcedureRequestResponseDTO execute(String professionalUserId, CreateProcedureRequestDTO dto);
}
