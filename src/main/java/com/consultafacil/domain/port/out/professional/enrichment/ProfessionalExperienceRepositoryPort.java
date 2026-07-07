package com.consultafacil.domain.port.out.professional.enrichment;

import com.consultafacil.domain.entity.ProfessionalExperience;

import java.util.List;
import java.util.Optional;

public interface ProfessionalExperienceRepositoryPort {
    ProfessionalExperience save(ProfessionalExperience experience);
    List<ProfessionalExperience> findByProfessionalProfileId(String professionalProfileId);
    Optional<ProfessionalExperience> findById(String id);
    void delete(ProfessionalExperience experience);
}
