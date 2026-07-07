package com.consultafacil.application.service.plan;

import com.consultafacil.api.dto.plan.PlanResponseDTO;
import com.consultafacil.api.dto.plan.UpdatePlanDTO;
import com.consultafacil.application.port.in.UpdatePlanUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.port.out.PlanRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePlanService implements UpdatePlanUseCase {

    private final PlanRepositoryPort planRepository;
    private final PlanMapper mapper;

    @Override
    @Transactional
    public PlanResponseDTO execute(String id, UpdatePlanDTO dto) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", id));

        if (dto.getName() != null) plan.setName(dto.getName());
        if (dto.getDescription() != null) plan.setDescription(dto.getDescription());
        if (dto.getPrice() != null) plan.setPrice(dto.getPrice());
        if (dto.getStatus() != null) plan.setStatus(dto.getStatus());
        if (dto.getDisplayOrder() != null) plan.setDisplayOrder(dto.getDisplayOrder());
        if (dto.getFeatures() != null) plan.setFeatures(String.join(",", dto.getFeatures()));
        if (dto.getMaxAppointments() != null) plan.setMaxAppointments(dto.getMaxAppointments());

        return mapper.toDTO(planRepository.save(plan));
    }
}
