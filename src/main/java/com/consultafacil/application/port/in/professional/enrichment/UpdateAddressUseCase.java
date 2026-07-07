package com.consultafacil.application.port.in.professional.enrichment;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.professional.UpdateAddressDTO;

public interface UpdateAddressUseCase {

    ProfessionalResponseDTO execute(String userId, UpdateAddressDTO dto);
}
