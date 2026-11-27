package com.consultafacil.application.port.in.insurance;

import com.consultafacil.api.dto.insurance.ProfessionalInsuranceDTO;

import java.util.List;

public interface GetProfessionalInsuranceUseCase {

    List<ProfessionalInsuranceDTO> execute(String professionalId);
}
