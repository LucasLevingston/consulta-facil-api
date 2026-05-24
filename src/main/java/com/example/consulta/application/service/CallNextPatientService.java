package com.example.consulta.application.service;

import com.example.consulta.api.dto.appointment.AppointmentResponseDTO;
import com.example.consulta.core.exception.BadRequestException;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.Appointment;
import com.example.consulta.domain.enums.AppointmentStatus;
import com.example.consulta.domain.repository.AppointmentRepository;
import com.example.consulta.domain.repository.ProfessionalProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CallNextPatientService {

    private final AppointmentRepository appointmentRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;

    @Transactional
    public AppointmentResponseDTO execute(String appointmentId, String professionalUserId) {
        var profile = professionalProfileRepository.findByUserId(professionalUserId)
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile", professionalUserId));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        if (!appointment.getProfessional().getId().equals(profile.getId())) {
            throw new BadRequestException("Appointment does not belong to this professional");
        }

        if (appointment.getStatus() != AppointmentStatus.CHECKED_IN) {
            throw new BadRequestException("Patient has not checked in yet");
        }

        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        appointment.setCalledAt(LocalDateTime.now());
        Appointment saved = appointmentRepository.save(appointment);

        return toResponseDTO(saved);
    }

    private AppointmentResponseDTO toResponseDTO(Appointment a) {
        return AppointmentResponseDTO.builder()
                .id(a.getId())
                .patientName(a.getPatient().getUser().getName())
                .patientId(a.getPatient().getId())
                .professionalName(a.getProfessional().getUser().getName())
                .professionalId(a.getProfessional().getId())
                .specialty(a.getProfessional().getSpecialty())
                .scheduledAt(a.getScheduledAt())
                .checkedInAt(a.getCheckedInAt())
                .calledAt(a.getCalledAt())
                .reason(a.getReason())
                .notes(a.getNotes())
                .status(a.getStatus())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
