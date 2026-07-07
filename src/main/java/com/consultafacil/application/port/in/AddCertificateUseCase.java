package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.professional.ProfessionalCertificateDTO;
import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;

public interface AddCertificateUseCase {

    ProfessionalResponseDTO execute(String userId, ProfessionalCertificateDTO dto);
}
