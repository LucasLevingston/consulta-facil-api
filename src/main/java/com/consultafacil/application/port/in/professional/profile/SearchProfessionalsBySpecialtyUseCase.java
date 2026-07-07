package com.consultafacil.application.port.in.professional.profile;

import com.consultafacil.api.dto.professional.ProfessionalResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchProfessionalsBySpecialtyUseCase {

    Page<ProfessionalResponseDTO> execute(String specialty, Pageable pageable);
}
