package com.consultafacil.application.port.in;

import java.util.Map;

public interface GetPatientMedicalRecordsUseCase {
    Map<String, Object> execute(String userId);
}
