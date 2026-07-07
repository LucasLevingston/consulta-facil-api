package com.consultafacil.application.port.in.systemfee;

import com.consultafacil.api.dto.billing.systemfee.SystemFeeResponseDTO;

public interface GetSystemFeeByIdUseCase {

    SystemFeeResponseDTO execute(String id);
}
