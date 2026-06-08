package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.procedurerequest.ProcedureRequestResponseDTO;

import java.util.List;

public interface GetPatientProcedureRequestsUseCase {

    List<ProcedureRequestResponseDTO> execute(String patientUserId);
}
