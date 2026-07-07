package com.consultafacil.application.port.in.patient;

import com.consultafacil.api.dto.patient.PatientVaccineDTO;

import java.util.List;

public interface ListVaccinesUseCase {

    List<PatientVaccineDTO> execute(String userId);
}
