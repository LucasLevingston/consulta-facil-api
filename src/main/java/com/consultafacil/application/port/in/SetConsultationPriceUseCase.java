package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;

import java.math.BigDecimal;

public interface SetConsultationPriceUseCase {

    ProfessionalResponseDTO execute(String userId, BigDecimal price);
}
