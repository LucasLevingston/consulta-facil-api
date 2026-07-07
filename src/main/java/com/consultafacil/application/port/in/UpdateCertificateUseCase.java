package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.professional.ProfessionalCertificateDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;

public interface UpdateCertificateUseCase {

    ProfessionalResponseDTO execute(String userId, String certificateId, ProfessionalCertificateDTO dto);
}
