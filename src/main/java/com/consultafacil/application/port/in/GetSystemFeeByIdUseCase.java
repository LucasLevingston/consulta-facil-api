package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.systemfee.SystemFeeResponseDTO;

public interface GetSystemFeeByIdUseCase {

    SystemFeeResponseDTO execute(String id);
}
