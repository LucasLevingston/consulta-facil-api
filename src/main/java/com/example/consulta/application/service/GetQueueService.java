package com.example.consulta.application.service;

import com.example.consulta.api.dto.appointment.AppointmentResponseDTO;
import com.example.consulta.core.exception.ResourceNotFoundException;
import com.example.consulta.domain.entity.Appointment;
import com.example.consulta.domain.enums.AppointmentStatus;
import com.example.consulta.domain.repository.AppointmentRepository;
import com.example.consulta.domain.repository.ProfessionalProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetQueueService {

    private final AppointmentRepository appointmentRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;

    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> execute(String professionalUserId) {
        var profile = professionalProfileRepository.findByUserId(professionalUserId)
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile", professionalUserId));

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return appointmentRepository
                .findByProfessionalIdAndStatusInAndScheduledAtBetweenOrderByCheckedInAt(
                        profile.getId(),
                        List.of(AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_PROGRESS),
                        startOfDay,
                        endOfDay)
                .stream()
                .map(this::toResponseDTO)
                .toList();
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
                .modality(a.getModality())
                .meetLink(a.getMeetLink())
                .status(a.getStatus())
                .paymentStatus(a.getPaymentStatus())
                .paymentAmount(a.getPaymentAmount())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
