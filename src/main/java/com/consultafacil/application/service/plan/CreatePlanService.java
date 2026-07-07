package com.consultafacil.application.service.plan;

import com.consultafacil.api.dto.plan.CreatePlanDTO;
import com.consultafacil.api.dto.plan.PlanResponseDTO;
import com.consultafacil.application.port.in.plan.CreatePlanUseCase;
import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.enums.PlanStatus;
import com.consultafacil.domain.port.out.plan.PlanRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreatePlanService implements CreatePlanUseCase {

    private final PlanRepositoryPort planRepository;
    private final PlanMapper mapper;

    @Override
    @Transactional
    public PlanResponseDTO execute(CreatePlanDTO dto) {
        String featuresStr = dto.getFeatures() != null
                ? String.join(",", dto.getFeatures())
                : null;

        Plan plan = Plan.builder()
                .slug(dto.getSlug().trim().toLowerCase())
                .name(dto.getName())
                .description(dto.getDescription())
                .tier(dto.getTier().toUpperCase())
                .billingPeriod(dto.getBillingPeriod())
                .price(dto.getPrice())
                .frequency(dto.getFrequency() > 0 ? dto.getFrequency() : 1)
                .frequencyType(dto.getFrequencyType() != null ? dto.getFrequencyType() : "months")
                .features(featuresStr)
                .maxAppointments(dto.getMaxAppointments())
                .status(PlanStatus.ACTIVE)
                .displayOrder(dto.getDisplayOrder())
                .build();

        return mapper.toDTO(planRepository.save(plan));
    }
}
