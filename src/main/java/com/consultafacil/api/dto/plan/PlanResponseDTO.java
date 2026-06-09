package com.consultafacil.api.dto.plan;

import com.consultafacil.domain.enums.BillingPeriod;
import com.consultafacil.domain.enums.PlanStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PlanResponseDTO {
    private String id;
    private String slug;
    private String name;
    private String description;
    private String tier;
    private BillingPeriod billingPeriod;
    private BigDecimal price;
    private int frequency;
    private String frequencyType;
    private List<String> features;
    private PlanStatus status;
    private int displayOrder;
}
