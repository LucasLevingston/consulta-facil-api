package com.consultafacil.domain.repository;

import com.consultafacil.domain.entity.ProfessionalExperience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfessionalExperienceRepository extends JpaRepository<ProfessionalExperience, String> {
    List<ProfessionalExperience> findByProfessionalProfileId(String professionalProfileId);
}
