package com.consultafacil.domain.repository.plan;

import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.enums.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, String> {

    Optional<Plan> findBySlug(String slug);

    List<Plan> findAllByStatusOrderByDisplayOrderAsc(PlanStatus status);
}
