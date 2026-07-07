package com.consultafacil.application.port.in.procedurerequest;

import com.consultafacil.api.dto.procedurerequest.ProcedureRequestResponseDTO;
import com.consultafacil.api.dto.procedurerequest.ScheduleProcedureRequestDTO;

public interface ScheduleProcedureRequestUseCase {

    ProcedureRequestResponseDTO execute(String requestId, String patientUserId,
                                        ScheduleProcedureRequestDTO dto);
}
