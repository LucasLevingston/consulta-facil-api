package com.consultafacil.application.port.in.professional.enrichment;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;

public interface DeleteCertificateUseCase {

    ProfessionalResponseDTO execute(String userId, String certificateId);
}
