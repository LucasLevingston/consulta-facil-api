package com.consultafacil.domain.repository.professional.enrichment;

import com.consultafacil.domain.entity.ProfessionalEducation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfessionalEducationRepository extends JpaRepository<ProfessionalEducation, String> {
    List<ProfessionalEducation> findByProfessionalProfileId(String professionalProfileId);
}
