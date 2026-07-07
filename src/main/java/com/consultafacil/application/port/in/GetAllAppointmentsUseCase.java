package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetAllAppointmentsUseCase {

    Page<AppointmentResponseDTO> execute(Pageable pageable);
}
