package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.appointment.PatientSummaryDTO;
import org.springframework.data.domain.Page;

public interface GetProfessionalPatientsUseCase {

    Page<PatientSummaryDTO> execute(String professionalId, String search, String sort, int page, int size);
}
