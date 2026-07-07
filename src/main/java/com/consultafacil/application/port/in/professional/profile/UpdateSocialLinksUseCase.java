package com.consultafacil.application.port.in.professional.profile;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import com.consultafacil.api.dto.professional.UpdateSocialLinksDTO;

public interface UpdateSocialLinksUseCase {

    ProfessionalResponseDTO execute(String userId, UpdateSocialLinksDTO dto);
}
