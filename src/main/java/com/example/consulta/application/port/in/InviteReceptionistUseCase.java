package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.receptionist.InviteReceptionistDTO;
import com.example.consulta.api.dto.receptionist.ReceptionistResponseDTO;

public interface InviteReceptionistUseCase {

    ReceptionistResponseDTO execute(String clinicId, String ownerUserId, InviteReceptionistDTO dto);
}
