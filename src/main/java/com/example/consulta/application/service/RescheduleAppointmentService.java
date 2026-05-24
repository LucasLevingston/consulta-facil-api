package com.example.consulta.application.service;

import com.example.consulta.api.dto.appointment.AppointmentResponseDTO;
import com.example.consulta.api.dto.appointment.RescheduleAppointmentDTO;
import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.Appointment;
import com.example.consulta.domain.enums.AppointmentStatus;
import com.example.consulta.domain.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RescheduleAppointmentService {

    private final AppointmentRepository appointmentRepository;

    @Transactional
    public AppointmentResponseDTO execute(String appointmentId, RescheduleAppointmentDTO dto) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        if (appointment.getStatus() != AppointmentStatus.PENDING
                && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BadRequestException(
                    "Only PENDING or CONFIRMED appointments can be rescheduled. Current status: "
                            + appointment.getStatus());
        }

        if (appointmentRepository.existsByProfessionalIdAndScheduledAt(
                appointment.getProfessional().getId(), dto.getScheduledAt())) {
            throw new BadRequestException("Professional already has an appointment scheduled at this time");
        }

        appointment.setPreviousScheduledAt(appointment.getScheduledAt());
        appointment.setScheduledAt(dto.getScheduledAt());
        if (dto.getReason() != null) {
            appointment.setReason(dto.getReason());
        }

        Appointment saved = appointmentRepository.save(appointment);
        return toResponseDTO(saved);
    }

    private AppointmentResponseDTO toResponseDTO(Appointment appointment) {
        return AppointmentResponseDTO.builder()
                .id(appointment.getId())
                .patientName(appointment.getPatient().getUser().getName())
                .patientId(appointment.getPatient().getId())
                .professionalName(appointment.getProfessional().getUser().getName())
                .professionalId(appointment.getProfessional().getId())
                .specialty(appointment.getProfessional().getSpecialty())
                .scheduledAt(appointment.getScheduledAt())
                .previousScheduledAt(appointment.getPreviousScheduledAt())
                .checkedInAt(appointment.getCheckedInAt())
                .calledAt(appointment.getCalledAt())
                .reason(appointment.getReason())
                .notes(appointment.getNotes())
                .status(appointment.getStatus())
                .cancellationReason(appointment.getCancellationReason())
                .rating(appointment.getRating())
                .ratingComment(appointment.getRatingComment())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
}
