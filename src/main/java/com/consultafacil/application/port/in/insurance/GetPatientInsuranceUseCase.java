package com.consultafacil.application.port.in.insurance;

import com.consultafacil.api.dto.insurance.PatientInsuranceDTO;

import java.util.List;

public interface GetPatientInsuranceUseCase {

    List<PatientInsuranceDTO> execute(String patientId);
}
