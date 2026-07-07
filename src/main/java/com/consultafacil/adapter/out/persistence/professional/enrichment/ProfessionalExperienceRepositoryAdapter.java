package com.consultafacil.adapter.out.persistence.professional.enrichment;

import com.consultafacil.domain.entity.ProfessionalExperience;
import com.consultafacil.domain.port.out.professional.enrichment.ProfessionalExperienceRepositoryPort;
import com.consultafacil.domain.repository.professional.enrichment.ProfessionalExperienceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProfessionalExperienceRepositoryAdapter implements ProfessionalExperienceRepositoryPort {

    private final ProfessionalExperienceRepository repository;

    @Override
    public ProfessionalExperience save(ProfessionalExperience experience) {
        return repository.save(experience);
    }

    @Override
    public List<ProfessionalExperience> findByProfessionalProfileId(String professionalProfileId) {
        return repository.findByProfessionalProfileId(professionalProfileId);
    }

    @Override
    public Optional<ProfessionalExperience> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public void delete(ProfessionalExperience experience) {
        repository.delete(experience);
    }
}
