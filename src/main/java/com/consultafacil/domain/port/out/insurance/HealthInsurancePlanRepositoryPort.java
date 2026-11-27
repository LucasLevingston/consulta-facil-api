package com.consultafacil.domain.port.out.insurance;

import com.consultafacil.domain.entity.HealthInsurancePlan;

import java.util.List;
import java.util.Optional;

public interface HealthInsurancePlanRepositoryPort {

    List<HealthInsurancePlan> findAllActive();

    Optional<HealthInsurancePlan> findById(String id);
}
