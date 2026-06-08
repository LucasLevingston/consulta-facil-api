package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.ProfessionalService;

import java.util.List;
import java.util.Optional;

public interface ProfessionalServiceRepositoryPort {

    ProfessionalService save(ProfessionalService service);

    Optional<ProfessionalService> findById(String id);

    List<ProfessionalService> findByProfessionalIdAndActiveTrue(String professionalId);
}
