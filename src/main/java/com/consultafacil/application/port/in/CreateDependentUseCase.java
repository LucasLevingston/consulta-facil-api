package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.dependent.CreateDependentDTO;
import com.consultafacil.api.dto.dependent.DependentResponseDTO;

public interface CreateDependentUseCase {

    DependentResponseDTO execute(String guardianUserId, CreateDependentDTO dto);
}
