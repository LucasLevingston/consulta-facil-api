package com.consultafacil.application.port.in.appointment;

import com.consultafacil.api.dto.appointment.CreateWalkInAppointmentDTO;
import com.consultafacil.api.dto.appointment.WalkInAppointmentResponseDTO;

public interface WalkInAppointmentUseCase {
    WalkInAppointmentResponseDTO create(String authenticatedUserId, CreateWalkInAppointmentDTO dto);
}
