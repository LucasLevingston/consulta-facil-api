package com.consultafacil.adapter.out.persistence.professional.enrichment;

import com.consultafacil.domain.entity.ProfessionalEducation;
import com.consultafacil.domain.port.out.professional.enrichment.ProfessionalEducationRepositoryPort;
import com.consultafacil.domain.repository.professional.enrichment.ProfessionalEducationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProfessionalEducationRepositoryAdapter implements ProfessionalEducationRepositoryPort {

    private final ProfessionalEducationRepository repository;

    @Override
    public ProfessionalEducation save(ProfessionalEducation education) {
        return repository.save(education);
    }

    @Override
    public List<ProfessionalEducation> findByProfessionalProfileId(String professionalProfileId) {
        return repository.findByProfessionalProfileId(professionalProfileId);
    }

    @Override
    public Optional<ProfessionalEducation> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public void delete(ProfessionalEducation education) {
        repository.delete(education);
    }
}
