package com.consultafacil.application.service.appointment;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.application.port.in.appointment.RescheduleAppointmentUseCase;
import com.consultafacil.application.port.in.command.RescheduleAppointmentCommand;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.core.security.OwnershipValidator;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RescheduleAppointmentService implements RescheduleAppointmentUseCase {

    private final AppointmentRepositoryPort appointmentRepository;
    private final OwnershipValidator ownershipValidator;

    @Override
    @Transactional
    public AppointmentResponseDTO execute(RescheduleAppointmentCommand command) {
        Appointment appointment = appointmentRepository.findById(command.appointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", command.appointmentId()));

        ownershipValidator.verifyAppointmentAccess(appointment, command.authenticatedUserId());

        if (appointmentRepository.existsByProfessionalIdAndScheduledAt(
                appointment.getProfessional().getId(), command.newScheduledAt())) {
            throw new BadRequestException("Professional already has an appointment scheduled at this time");
        }

        appointment.reschedule(command.newScheduledAt(), command.newReason());

        return toResponseDTO(appointmentRepository.save(appointment));
    }

    // --- backward-compat bridge used by tests that still build the old DTO ---
    @Transactional
    public AppointmentResponseDTO execute(String appointmentId, String authenticatedUserId,
                                          com.consultafacil.api.dto.appointment.RescheduleAppointmentDTO dto) {
        return execute(new RescheduleAppointmentCommand(
                appointmentId, authenticatedUserId, dto.getScheduledAt(), dto.getReason()));
    }

    private AppointmentResponseDTO toResponseDTO(Appointment appointment) {
        return AppointmentResponseDTO.builder()
                .id(appointment.getId())
                .patientName(appointment.getPatient().getUser().getName())
                .patientId(appointment.getPatient().getId())
                .professionalName(appointment.getProfessional().getUser().getName())
                .professionalId(appointment.getProfessional().getId())
                .specialty(appointment.getProfessional().getSpecialty() != null ? appointment.getProfessional().getSpecialty().name() : null)
                .scheduledAt(appointment.getScheduledAt())
                .previousScheduledAt(appointment.getPreviousScheduledAt())
                .checkedInAt(appointment.getCheckedInAt())
                .calledAt(appointment.getCalledAt())
                .reason(appointment.getReason())
                .notes(appointment.getNotes())
                .modality(appointment.getModality())
                .meetLink(appointment.getMeetLink())
                .status(appointment.getStatus())
                .cancellationReason(appointment.getCancellationReason())
                .paymentStatus(appointment.getPaymentStatus())
                .paymentAmount(appointment.getPaymentAmount())
                .rating(appointment.getRating())
                .ratingComment(appointment.getRatingComment())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
}
