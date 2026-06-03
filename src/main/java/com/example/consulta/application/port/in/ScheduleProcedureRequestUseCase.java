package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.procedurerequest.ProcedureRequestResponseDTO;
import com.example.consulta.api.dto.procedurerequest.ScheduleProcedureRequestDTO;

public interface ScheduleProcedureRequestUseCase {

    ProcedureRequestResponseDTO execute(String requestId, String patientUserId,
                                        ScheduleProcedureRequestDTO dto);
}
