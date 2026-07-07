package com.consultafacil.application.port.in.professional.profile;

import com.consultafacil.api.dto.professional.ProfessionalRatingDTO;

public interface GetProfessionalRatingsUseCase {

    ProfessionalRatingDTO execute(String professionalId);
}
