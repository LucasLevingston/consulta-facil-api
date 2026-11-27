package com.consultafacil.domain.port.out.insurance;

import com.consultafacil.domain.entity.PatientInsurance;

import java.util.List;

public interface PatientInsuranceRepositoryPort {

    PatientInsurance save(PatientInsurance entity);

    List<PatientInsurance> findByPatientId(String patientId);
}
