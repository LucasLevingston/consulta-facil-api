package com.example.consulta.application.port.in;

import com.example.consulta.api.dto.appointment.AppointmentResponseDTO;
import com.example.consulta.api.dto.appointment.PatientSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppointmentQueryUseCase {

    AppointmentResponseDTO getById(String appointmentId, String authenticatedUserId);

    Page<AppointmentResponseDTO> getPatientAppointments(String userId, String authenticatedUserId,
                                                         boolean isAdmin, Pageable pageable);

    Page<AppointmentResponseDTO> getProfessionalAppointments(String professionalId, Pageable pageable);

    Page<PatientSummaryDTO> getProfessionalPatients(String professionalId, String search,
                                                     String sort, int page, int size);
}
