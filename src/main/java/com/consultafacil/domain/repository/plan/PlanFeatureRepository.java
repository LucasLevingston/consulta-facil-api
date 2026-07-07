package com.consultafacil.domain.repository.plan;

import com.consultafacil.domain.entity.PlanFeature;
import com.consultafacil.domain.entity.PlanFeatureId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanFeatureRepository extends JpaRepository<PlanFeature, PlanFeatureId> {
    List<PlanFeature> findByIdPlanId(String planId);
    void deleteByIdPlanId(String planId);
}
