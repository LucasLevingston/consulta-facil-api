package com.consultafacil.application.port.in.dependent;

import com.consultafacil.api.dto.dependent.DependentResponseDTO;

import java.util.List;

public interface ListDependentsByGuardianUseCase {

    List<DependentResponseDTO> execute(String guardianUserId);
}
