package com.consultafacil.application.port.in;

import java.util.Map;

public interface PatientProfileUseCase {

    Map<String, Object> getPatientProfile(String userId);

    Map<String, Object> updatePatientProfile(String userId, Map<String, Object> updates);

    Map<String, Object> getPatientMedicalRecords(String userId);

    Map<String, Object> updatePatientMedicalRecords(String userId, Map<String, Object> updates);
}
