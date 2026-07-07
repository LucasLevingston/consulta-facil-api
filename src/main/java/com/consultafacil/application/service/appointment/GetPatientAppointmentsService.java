package com.consultafacil.application.service.appointment;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.application.port.in.appointment.GetPatientAppointmentsUseCase;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.patient.PatientProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetPatientAppointmentsService implements GetPatientAppointmentsUseCase {

    private final PatientProfileRepositoryPort patientProfileRepository;
    private final AppointmentRepositoryPort appointmentRepository;
    private final AppointmentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> execute(String userId, String authenticatedUserId,
                                                 boolean isAdmin, Pageable pageable) {
        if (!isAdmin && !userId.equals(authenticatedUserId)) {
            throw new AccessDeniedException("You can only view your own appointments");
        }
        return patientProfileRepository.findByUserId(userId)
                .map(patient -> appointmentRepository.findByPatientId(patient.getId(), pageable)
                        .map(mapper::toResponseDTO))
                .orElse(Page.empty(pageable));
    }
}
