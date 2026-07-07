package com.consultafacil.domain.port.out.plan;

import com.consultafacil.domain.entity.PlanFeature;

import java.util.List;

public interface PlanFeatureRepositoryPort {
    PlanFeature save(PlanFeature planFeature);
    List<PlanFeature> findByPlanId(String planId);
    void deleteByPlanId(String planId);
}
