package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.professional.ProfessionalResponseDTO;

import java.math.BigDecimal;

public interface SetConsultationPriceUseCase {

    ProfessionalResponseDTO execute(String userId, BigDecimal price);
}
