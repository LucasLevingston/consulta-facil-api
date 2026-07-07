package com.consultafacil.domain.port.out.plan;

import com.consultafacil.domain.entity.Plan;

import java.util.List;
import java.util.Optional;

public interface PlanRepositoryPort {
    Plan save(Plan plan);
    Optional<Plan> findById(String id);
    Optional<Plan> findBySlug(String slug);
    List<Plan> findAll();
    List<Plan> findAllActiveOrderByDisplayOrder();
}
