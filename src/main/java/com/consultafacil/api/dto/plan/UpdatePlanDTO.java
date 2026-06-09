package com.consultafacil.api.dto.plan;

import com.consultafacil.domain.enums.PlanStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdatePlanDTO {
    private String name;
    private String description;
    private BigDecimal price;
    private List<String> features;
    private PlanStatus status;
    private Integer displayOrder;
}
