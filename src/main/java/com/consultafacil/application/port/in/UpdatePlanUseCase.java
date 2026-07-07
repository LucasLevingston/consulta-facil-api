package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.plan.PlanResponseDTO;
import com.consultafacil.api.dto.plan.UpdatePlanDTO;

public interface UpdatePlanUseCase {

    PlanResponseDTO execute(String id, UpdatePlanDTO dto);
}
