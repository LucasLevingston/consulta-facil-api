package com.consultafacil.application.port.in.insurance;

import com.consultafacil.api.dto.insurance.AddProfessionalInsuranceRequest;

public interface AddProfessionalInsuranceUseCase {

    void execute(String professionalId, AddProfessionalInsuranceRequest request);
}
