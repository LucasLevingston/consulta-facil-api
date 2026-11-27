package com.consultafacil.adapter.out.persistence.insurance;

import com.consultafacil.domain.entity.HealthInsurancePlan;
import com.consultafacil.domain.port.out.insurance.HealthInsurancePlanRepositoryPort;
import com.consultafacil.domain.repository.insurance.HealthInsurancePlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HealthInsurancePlanRepositoryAdapter implements HealthInsurancePlanRepositoryPort {

    private final HealthInsurancePlanRepository repository;

    @Override
    public List<HealthInsurancePlan> findAllActive() {
        return repository.findByActiveTrueOrderByName();
    }

    @Override
    public Optional<HealthInsurancePlan> findById(String id) {
        return repository.findById(id);
    }
}
