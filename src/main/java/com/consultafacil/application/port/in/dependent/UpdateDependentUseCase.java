package com.consultafacil.application.port.in.dependent;

import com.consultafacil.api.dto.dependent.DependentResponseDTO;
import com.consultafacil.api.dto.dependent.UpdateDependentDTO;

public interface UpdateDependentUseCase {

    DependentResponseDTO execute(String dependentId, String guardianUserId, UpdateDependentDTO dto);
}
