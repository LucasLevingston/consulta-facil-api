package com.consultafacil.application.port.in.patient;

import java.util.Map;

public interface UpdatePatientMedicalRecordsUseCase {
    Map<String, Object> execute(String userId, Map<String, Object> updates);
}
