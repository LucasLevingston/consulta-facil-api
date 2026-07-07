package com.consultafacil.adapter.out.persistence.plan;

import com.consultafacil.domain.entity.PlanFeature;
import com.consultafacil.domain.port.out.plan.PlanFeatureRepositoryPort;
import com.consultafacil.domain.repository.plan.PlanFeatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PlanFeatureRepositoryAdapter implements PlanFeatureRepositoryPort {

    private final PlanFeatureRepository planFeatureRepository;

    @Override
    public PlanFeature save(PlanFeature planFeature) { return planFeatureRepository.save(planFeature); }

    @Override
    public List<PlanFeature> findByPlanId(String planId) { return planFeatureRepository.findByIdPlanId(planId); }

    @Override
    public void deleteByPlanId(String planId) { planFeatureRepository.deleteByIdPlanId(planId); }
}
