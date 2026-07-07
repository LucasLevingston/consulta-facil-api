package com.consultafacil.application.service.plan;

import com.consultafacil.api.dto.plan.PlanResponseDTO;
import com.consultafacil.application.port.in.GetPlanBySlugUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.port.out.PlanRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetPlanBySlugService implements GetPlanBySlugUseCase {

    private final PlanRepositoryPort planRepository;
    private final PlanMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PlanResponseDTO execute(String slug) {
        return planRepository.findBySlug(slug)
                .map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", slug));
    }
}
