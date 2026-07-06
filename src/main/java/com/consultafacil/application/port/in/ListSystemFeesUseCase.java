package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.systemfee.SystemFeeResponseDTO;

import java.util.List;

public interface ListSystemFeesUseCase {

    List<SystemFeeResponseDTO> execute();
}
