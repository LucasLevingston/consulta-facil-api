package com.consultafacil.application.port.in.systemfee;

import com.consultafacil.api.dto.billing.systemfee.SystemFeeResponseDTO;
import com.consultafacil.api.dto.billing.systemfee.UpdateSystemFeeDTO;

public interface UpdateSystemFeeUseCase {

    SystemFeeResponseDTO execute(String id, UpdateSystemFeeDTO dto);
}
