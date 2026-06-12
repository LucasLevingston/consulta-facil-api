package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.systemfee.SystemFeeResponseDTO;
import com.consultafacil.api.dto.billing.systemfee.UpdateSystemFeeDTO;

import java.util.List;

public interface SystemFeeUseCase {
    List<SystemFeeResponseDTO> listAll();
    SystemFeeResponseDTO getById(String id);
    SystemFeeResponseDTO update(String id, UpdateSystemFeeDTO dto);
}
