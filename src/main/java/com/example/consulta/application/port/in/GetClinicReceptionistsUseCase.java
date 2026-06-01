package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.receptionist.ReceptionistResponseDTO;

import java.util.List;

public interface GetClinicReceptionistsUseCase {

    List<ReceptionistResponseDTO> execute(String clinicId, String requestingUserId);
}
