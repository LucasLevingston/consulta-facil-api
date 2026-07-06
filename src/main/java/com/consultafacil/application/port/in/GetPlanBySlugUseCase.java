package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.plan.PlanResponseDTO;

public interface GetPlanBySlugUseCase {

    PlanResponseDTO execute(String slug);
}
