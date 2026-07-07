package com.consultafacil.application.service.appointment;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.application.observability.BusinessMetrics;
import com.consultafacil.application.port.in.CancelAppointmentUseCase;
import com.consultafacil.application.port.in.command.CancelAppointmentCommand;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.port.out.AppointmentNotificationPort;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import com.consultafacil.core.security.OwnershipValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelAppointmentService implements CancelAppointmentUseCase {
    private final AppointmentRepositoryPort appointmentRepository;
    private final AppointmentNotificationPort appointmentNotification;
    private final OwnershipValidator ownershipValidator;
    private final BusinessMetrics businessMetrics;
    private final AppointmentMapper mapper;

    @Override
    @Transactional
    public AppointmentResponseDTO execute(CancelAppointmentCommand command) {
        Appointment appointment = appointmentRepository.findById(command.appointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", command.appointmentId()));
        ownershipValidator.verifyAppointmentAccess(appointment, command.authenticatedUserId());
        appointment.cancel(command.cancellationReason());
        Appointment updated = appointmentRepository.save(appointment);
        appointmentNotification.notifyCanceled(updated);
        businessMetrics.recordAppointmentCanceled();
        return mapper.toResponseDTO(updated);
    }
}
