package com.consultafacil.application.port.in.insurance;

import com.consultafacil.api.dto.insurance.PatientInsuranceDTO;
import com.consultafacil.api.dto.insurance.RegisterPatientInsuranceRequest;

public interface RegisterPatientInsuranceUseCase {

    PatientInsuranceDTO execute(String patientId, RegisterPatientInsuranceRequest request);
}
