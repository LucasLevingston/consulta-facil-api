package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.ProfessionalEducation;

import java.util.List;
import java.util.Optional;

public interface ProfessionalEducationRepositoryPort {
    ProfessionalEducation save(ProfessionalEducation education);
    List<ProfessionalEducation> findByProfessionalProfileId(String professionalProfileId);
    Optional<ProfessionalEducation> findById(String id);
    void delete(ProfessionalEducation education);
}
