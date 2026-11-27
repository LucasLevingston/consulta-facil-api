package com.consultafacil.adapter.out.persistence.insurance;

import com.consultafacil.domain.entity.ProfessionalInsuranceAccepted;
import com.consultafacil.domain.port.out.insurance.ProfessionalInsuranceRepositoryPort;
import com.consultafacil.domain.repository.insurance.ProfessionalInsuranceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProfessionalInsuranceRepositoryAdapter implements ProfessionalInsuranceRepositoryPort {

    private final ProfessionalInsuranceRepository repository;

    @Override
    public ProfessionalInsuranceAccepted save(ProfessionalInsuranceAccepted entity) {
        return repository.save(entity);
    }

    @Override
    public List<ProfessionalInsuranceAccepted> findByProfessionalId(String professionalId) {
        return repository.findByProfessionalId(professionalId);
    }

    @Override
    public void deleteByProfessionalIdAndPlanId(String professionalId, String planId) {
        repository.deleteByProfessionalIdAndPlanId(professionalId, planId);
    }
}
