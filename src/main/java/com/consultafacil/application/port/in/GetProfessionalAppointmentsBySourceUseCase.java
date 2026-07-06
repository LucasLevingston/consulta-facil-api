package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.domain.enums.AppointmentSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetProfessionalAppointmentsBySourceUseCase {

    Page<AppointmentResponseDTO> execute(String professionalId, AppointmentSource source, Pageable pageable);
}
