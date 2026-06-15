package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.dependent.CreateDependentDTO;
import com.consultafacil.api.dto.dependent.DependentResponseDTO;
import com.consultafacil.api.dto.dependent.UpdateDependentDTO;

import java.util.List;

public interface DependentUseCase {
    DependentResponseDTO create(String guardianUserId, CreateDependentDTO dto);
    List<DependentResponseDTO> listByGuardian(String guardianUserId);
    DependentResponseDTO update(String dependentId, String guardianUserId, UpdateDependentDTO dto);
    void delete(String dependentId, String guardianUserId);
}
