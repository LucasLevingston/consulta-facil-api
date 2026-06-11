package com.consultafacil.application.port.in;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface PatientProfileUseCase {

    Map<String, Object> getPatientProfile(String userId);

    Map<String, Object> updatePatientProfile(String userId, Map<String, Object> updates);

    Map<String, Object> getPatientMedicalRecords(String userId);

    Map<String, Object> updatePatientMedicalRecords(String userId, Map<String, Object> updates);

    Page<Map<String, Object>> getAllPatients(Pageable pageable);
}
