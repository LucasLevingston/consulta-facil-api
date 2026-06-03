package com.example.consulta.domain.port.out;

import com.example.consulta.domain.entity.MedicalRecord;

import java.util.Optional;

public interface MedicalRecordRepositoryPort {

    MedicalRecord save(MedicalRecord record);

    Optional<MedicalRecord> findByPatientProfileId(String patientProfileId);
}
