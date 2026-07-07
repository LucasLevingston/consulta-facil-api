package com.consultafacil.domain.port.out.patient;

import com.consultafacil.domain.entity.PatientVaccine;

import java.util.List;
import java.util.Optional;

public interface PatientVaccineRepositoryPort {

    PatientVaccine save(PatientVaccine vaccine);

    List<PatientVaccine> findByPatientProfileId(String patientProfileId);

    Optional<PatientVaccine> findById(String id);

    void delete(PatientVaccine vaccine);
}
