package com.consultafacil.application.port.in.plan;

import com.consultafacil.api.dto.plan.PlanResponseDTO;

import java.util.List;

public interface ListAllPlansUseCase {

    List<PlanResponseDTO> execute();
}
