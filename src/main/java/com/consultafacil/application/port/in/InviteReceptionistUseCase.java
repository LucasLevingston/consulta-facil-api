package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.receptionist.InviteReceptionistDTO;
import com.consultafacil.api.dto.receptionist.ReceptionistResponseDTO;

public interface InviteReceptionistUseCase {

    ReceptionistResponseDTO execute(String clinicId, String ownerUserId, InviteReceptionistDTO dto);
}
