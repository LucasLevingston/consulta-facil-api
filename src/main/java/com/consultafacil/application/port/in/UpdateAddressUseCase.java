package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.professional.UpdateAddressDTO;

public interface UpdateAddressUseCase {

    ProfessionalResponseDTO execute(String userId, UpdateAddressDTO dto);
}
