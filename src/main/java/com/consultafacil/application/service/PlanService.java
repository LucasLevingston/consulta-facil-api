package com.consultafacil.application.service;

import com.consultafacil.api.dto.plan.CreatePlanDTO;
import com.consultafacil.api.dto.plan.PlanResponseDTO;
import com.consultafacil.api.dto.plan.UpdatePlanDTO;
import com.consultafacil.application.port.in.PlanUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Plan;
import com.consultafacil.domain.enums.PlanStatus;
import com.consultafacil.domain.port.out.PlanRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanService implements PlanUseCase {

    private final PlanRepositoryPort planRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponseDTO> listActivePlans() {
        return planRepository.findAllActiveOrderByDisplayOrder().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponseDTO> listAllPlans() {
        return planRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PlanResponseDTO getBySlug(String slug) {
        return planRepository.findBySlug(slug)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", slug));
    }

    @Override
    @Transactional
    public PlanResponseDTO createPlan(CreatePlanDTO dto) {
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
                .status(PlanStatus.ACTIVE)
                .displayOrder(dto.getDisplayOrder())
                .build();

        return toDTO(planRepository.save(plan));
    }

    @Override
    @Transactional
    public PlanResponseDTO updatePlan(String id, UpdatePlanDTO dto) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", id));

        if (dto.getName() != null) plan.setName(dto.getName());
        if (dto.getDescription() != null) plan.setDescription(dto.getDescription());
        if (dto.getPrice() != null) plan.setPrice(dto.getPrice());
        if (dto.getStatus() != null) plan.setStatus(dto.getStatus());
        if (dto.getDisplayOrder() != null) plan.setDisplayOrder(dto.getDisplayOrder());
        if (dto.getFeatures() != null) plan.setFeatures(String.join(",", dto.getFeatures()));

        return toDTO(planRepository.save(plan));
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private PlanResponseDTO toDTO(Plan p) {
        List<String> featureList = p.getFeatures() != null && !p.getFeatures().isBlank()
                ? Arrays.stream(p.getFeatures().split(","))
                        .map(String::trim)
                        .collect(Collectors.toList())
                : Collections.emptyList();

        return PlanResponseDTO.builder()
                .id(p.getId())
                .slug(p.getSlug())
                .name(p.getName())
                .description(p.getDescription())
                .tier(p.getTier())
                .billingPeriod(p.getBillingPeriod())
                .price(p.getPrice())
                .frequency(p.getFrequency())
                .frequencyType(p.getFrequencyType())
                .features(featureList)
                .status(p.getStatus())
                .displayOrder(p.getDisplayOrder())
                .build();
    }
}
