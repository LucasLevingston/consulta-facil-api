package com.consultafacil.application.port.in.plan;

import com.consultafacil.api.dto.plan.CreatePlanDTO;
import com.consultafacil.api.dto.plan.PlanResponseDTO;

public interface CreatePlanUseCase {

    PlanResponseDTO execute(CreatePlanDTO dto);
}
