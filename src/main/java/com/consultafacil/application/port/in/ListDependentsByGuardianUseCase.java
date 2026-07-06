package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.dependent.DependentResponseDTO;

import java.util.List;

public interface ListDependentsByGuardianUseCase {

    List<DependentResponseDTO> execute(String guardianUserId);
}
