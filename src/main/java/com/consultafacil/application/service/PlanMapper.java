package com.consultafacil.application.service;

import com.consultafacil.api.dto.plan.PlanResponseDTO;
import com.consultafacil.domain.entity.Plan;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PlanMapper {

    public PlanResponseDTO toDTO(Plan p) {
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
                .consultationFeeRate(p.getConsultationFeeRate())
                .maxAppointments(p.getMaxAppointments())
                .status(p.getStatus())
                .displayOrder(p.getDisplayOrder())
                .build();
    }
}
