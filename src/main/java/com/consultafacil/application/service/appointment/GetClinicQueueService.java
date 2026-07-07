package com.consultafacil.application.service.appointment;

import com.consultafacil.api.dto.appointment.AppointmentResponseDTO;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import com.consultafacil.application.port.in.appointment.GetClinicQueueUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetClinicQueueService implements GetClinicQueueUseCase {

    private final AppointmentRepositoryPort appointmentRepository;

    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> execute(String clinicId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        List<AppointmentStatus> statuses = List.of(
                AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_PROGRESS);

        return appointmentRepository
                .findClinicQueueAppointments(clinicId, statuses, startOfDay, endOfDay)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private AppointmentResponseDTO toDTO(Appointment a) {
        return AppointmentResponseDTO.builder()
                .id(a.getId())
                .patientName(a.getPatient().getUser().getName())
                .patientId(a.getPatient().getId())
                .professionalName(a.getProfessional().getUser().getName())
                .professionalId(a.getProfessional().getId())
                .specialty(a.getProfessional().getSpecialty() != null ? a.getProfessional().getSpecialty().name() : null)
                .scheduledAt(a.getScheduledAt())
                .checkedInAt(a.getCheckedInAt())
                .calledAt(a.getCalledAt())
                .reason(a.getReason())
                .status(a.getStatus())
                .modality(a.getModality())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
