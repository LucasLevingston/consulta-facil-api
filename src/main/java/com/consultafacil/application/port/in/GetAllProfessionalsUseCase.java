package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetAllProfessionalsUseCase {

    Page<ProfessionalResponseDTO> execute(String profession, String specialty, String name, Pageable pageable);
}
