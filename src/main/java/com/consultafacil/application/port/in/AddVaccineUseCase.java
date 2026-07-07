package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.patient.PatientVaccineDTO;

public interface AddVaccineUseCase {

    PatientVaccineDTO execute(String userId, PatientVaccineDTO dto);
}
