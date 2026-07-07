package com.consultafacil.application.service.appointment;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.application.port.in.appointment.ConfirmAppointmentUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.core.security.OwnershipValidator;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.port.out.appointment.AppointmentNotificationPort;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfirmAppointmentService implements ConfirmAppointmentUseCase {
    private final AppointmentRepositoryPort appointmentRepository;
    private final AppointmentNotificationPort appointmentNotification;
    private final OwnershipValidator ownershipValidator;
    private final AppointmentMapper mapper;

    @Override
    @Transactional
    public AppointmentResponseDTO confirm(String appointmentId, String professionalUserId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        ownershipValidator.verifyProfessionalOwnership(appointment, professionalUserId);
        appointment.confirm();
        Appointment updated = appointmentRepository.save(appointment);
        appointmentNotification.notifyConfirmed(updated);
        return mapper.toResponseDTO(updated);
    }
}
