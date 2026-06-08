package com.consultafacil.application.service;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.application.port.in.GetQueueUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.port.out.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.ProfessionalProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetQueueService implements GetQueueUseCase {

    private final AppointmentRepositoryPort appointmentRepository;
    private final ProfessionalProfileRepositoryPort professionalProfileRepository;

    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> execute(String userId, String role) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        List<AppointmentStatus> statuses = List.of(AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_PROGRESS);

        List<Appointment> appointments;
        if ("ROLE_PROFESSIONAL".equals(role)) {
            var profile = professionalProfileRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile", userId));
            appointments = appointmentRepository
                    .findByProfessionalIdAndStatusInAndScheduledAtBetweenOrderByCheckedInAt(
                            profile.getId(), statuses, startOfDay, endOfDay);
        } else {
            appointments = appointmentRepository
                    .findByStatusInAndScheduledAtBetweenOrderByCheckedInAt(statuses, startOfDay, endOfDay);
        }

        return appointments.stream().map(this::toResponseDTO).toList();
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
