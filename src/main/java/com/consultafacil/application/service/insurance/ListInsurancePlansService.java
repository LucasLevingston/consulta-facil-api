package com.consultafacil.application.service.insurance;

import com.consultafacil.api.dto.insurance.HealthInsurancePlanDTO;
import com.consultafacil.application.port.in.insurance.ListInsurancePlansUseCase;
import com.consultafacil.domain.port.out.insurance.HealthInsurancePlanRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListInsurancePlansService implements ListInsurancePlansUseCase {

    private final HealthInsurancePlanRepositoryPort planRepository;

    @Override
    public List<HealthInsurancePlanDTO> execute() {
        return planRepository.findAllActive().stream()
                .map(plan -> new HealthInsurancePlanDTO(plan.getId(), plan.getName(), plan.getCode(), plan.isActive()))
                .toList();
    }
}
