package com.consultafacil.adapter.out.persistence;

import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.enums.PlanStatus;
import com.consultafacil.domain.port.out.PlanRepositoryPort;
import com.consultafacil.domain.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PlanRepositoryAdapter implements PlanRepositoryPort {

    private final PlanRepository planRepository;

    @Override
    public Plan save(Plan plan) {
        return planRepository.save(plan);
    }

    @Override
    public Optional<Plan> findById(String id) {
        return planRepository.findById(id);
    }

    @Override
    public Optional<Plan> findBySlug(String slug) {
        return planRepository.findBySlug(slug);
    }

    @Override
    public List<Plan> findAll() {
        return planRepository.findAll();
    }

    @Override
    public List<Plan> findAllActiveOrderByDisplayOrder() {
        return planRepository.findAllByStatusOrderByDisplayOrderAsc(PlanStatus.ACTIVE);
    }
}
