package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.plan.PlanResponseDTO;

import java.util.List;

public interface ListActivePlansUseCase {

    List<PlanResponseDTO> execute();
}
