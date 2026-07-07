package com.consultafacil.application.port.in;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface GetAllPatientsUseCase {
    Page<Map<String, Object>> execute(Pageable pageable);
}
