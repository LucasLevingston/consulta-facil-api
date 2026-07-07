package com.consultafacil.application.port.in;

import java.util.Map;

public interface GetPatientProfileUseCase {
    Map<String, Object> execute(String userId);
}
