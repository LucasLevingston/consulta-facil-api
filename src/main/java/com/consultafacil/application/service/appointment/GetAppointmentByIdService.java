package com.consultafacil.application.service.appointment;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.application.port.in.appointment.GetAppointmentByIdUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.core.security.OwnershipValidator;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAppointmentByIdService implements GetAppointmentByIdUseCase {

    private final AppointmentRepositoryPort appointmentRepository;
    private final OwnershipValidator ownershipValidator;
    private final AppointmentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponseDTO execute(String appointmentId, String authenticatedUserId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        ownershipValidator.verifyAppointmentAccess(appointment, authenticatedUserId);
        return mapper.toResponseDTO(appointment);
    }
}
