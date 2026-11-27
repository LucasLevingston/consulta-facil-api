package com.consultafacil.application.port.in.insurance;

import com.consultafacil.api.dto.insurance.HealthInsurancePlanDTO;

import java.util.List;

public interface ListInsurancePlansUseCase {

    List<HealthInsurancePlanDTO> execute();
}
