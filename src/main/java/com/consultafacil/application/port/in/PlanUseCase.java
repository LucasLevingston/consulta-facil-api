package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.plan.CreatePlanDTO;
import com.consultafacil.api.dto.plan.PlanResponseDTO;
import com.consultafacil.api.dto.plan.UpdatePlanDTO;

import java.util.List;

public interface PlanUseCase {

    List<PlanResponseDTO> listActivePlans();

    List<PlanResponseDTO> listAllPlans();

    PlanResponseDTO getBySlug(String slug);

    PlanResponseDTO createPlan(CreatePlanDTO dto);

    PlanResponseDTO updatePlan(String id, UpdatePlanDTO dto);
}
