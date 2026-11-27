package com.consultafacil.domain.repository.insurance;

import com.consultafacil.domain.entity.HealthInsurancePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthInsurancePlanRepository extends JpaRepository<HealthInsurancePlan, String> {

    List<HealthInsurancePlan> findByActiveTrueOrderByName();
}
