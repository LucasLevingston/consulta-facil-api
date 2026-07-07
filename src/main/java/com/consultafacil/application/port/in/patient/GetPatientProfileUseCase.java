package com.consultafacil.application.port.in.patient;

import java.util.Map;

public interface GetPatientProfileUseCase {
    Map<String, Object> execute(String userId);
}
