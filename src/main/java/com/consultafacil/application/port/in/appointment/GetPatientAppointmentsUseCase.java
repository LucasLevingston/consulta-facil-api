package com.consultafacil.application.port.in.appointment;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetPatientAppointmentsUseCase {

    Page<AppointmentResponseDTO> execute(String userId, String authenticatedUserId,
                                          boolean isAdmin, Pageable pageable);
}
