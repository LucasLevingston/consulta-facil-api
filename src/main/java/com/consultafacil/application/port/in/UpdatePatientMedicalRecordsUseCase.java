package com.consultafacil.application.port.in;

import java.util.Map;

public interface UpdatePatientMedicalRecordsUseCase {
    Map<String, Object> execute(String userId, Map<String, Object> updates);
}
