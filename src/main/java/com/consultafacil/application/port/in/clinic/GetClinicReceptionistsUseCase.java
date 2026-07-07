package com.consultafacil.application.port.in.clinic;

import com.consultafacil.api.dto.receptionist.ReceptionistResponseDTO;

import java.util.List;

public interface GetClinicReceptionistsUseCase {

    List<ReceptionistResponseDTO> execute(String clinicId, String requestingUserId);
}
