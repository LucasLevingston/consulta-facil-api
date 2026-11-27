package com.consultafacil.api.dto.subscription;

import com.consultafacil.domain.enums.SubscriptionPlan;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class SubscriptionPlanInfoDTO {
    private SubscriptionPlan plan;
    private String name;
    private BigDecimal monthlyPrice;
    private List<String> features;
    private int maxAppointmentsPerMonth; // -1 = unlimited
}
