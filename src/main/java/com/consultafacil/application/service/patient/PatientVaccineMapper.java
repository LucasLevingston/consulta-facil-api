package com.consultafacil.application.service.patient;

import com.consultafacil.api.dto.patient.PatientVaccineDTO;
import com.consultafacil.domain.entity.PatientVaccine;
import org.springframework.stereotype.Component;

@Component
public class PatientVaccineMapper {

    public PatientVaccineDTO toDTO(PatientVaccine v) {
        return new PatientVaccineDTO(v.getId(), v.getVaccineName(), v.getDoseNumber(), v.getAdministeredAt(), v.getNotes());
    }
}
