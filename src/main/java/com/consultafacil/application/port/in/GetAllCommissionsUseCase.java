package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.commission.ReferralCommissionDTO;

import java.util.List;

public interface GetAllCommissionsUseCase {
    List<ReferralCommissionDTO> execute();
}
