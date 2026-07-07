package com.consultafacil.domain.port.out.patient;

import com.consultafacil.domain.entity.MedicalRecord;

import java.util.Optional;

public interface MedicalRecordRepositoryPort {

    MedicalRecord save(MedicalRecord record);

    Optional<MedicalRecord> findByPatientProfileId(String patientProfileId);
}
