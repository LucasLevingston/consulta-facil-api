package com.consultafacil.application.port.in.professional.profile;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.professional.UpdateBioDTO;

public interface UpdateBioUseCase {

    ProfessionalResponseDTO execute(String userId, UpdateBioDTO dto);
}
