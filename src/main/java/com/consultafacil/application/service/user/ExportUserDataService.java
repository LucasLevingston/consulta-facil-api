package com.consultafacil.application.service.user;

import com.consultafacil.api.dto.user.AppointmentSummaryDTO;
import com.consultafacil.api.dto.user.UserDataExportDTO;
import com.consultafacil.application.port.in.user.ExportUserDataUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.PatientProfile;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.exception.RateLimitException;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.patient.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportUserDataService implements ExportUserDataUseCase {

    private final UserRepositoryPort userRepository;
    private final PatientProfileRepositoryPort patientProfileRepository;
    private final AppointmentRepositoryPort appointmentRepository;

    @Override
    @Transactional
    public UserDataExportDTO execute(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user.getLastDataExportAt() != null
                && user.getLastDataExportAt().isAfter(LocalDateTime.now().minusHours(1))) {
            throw new RateLimitException("EXPORT_RATE_LIMIT", "Aguarde 1 hora entre exportações de dados");
        }

        List<AppointmentSummaryDTO> appointmentSummaries = buildAppointmentSummaries(userId);

        user.setLastDataExportAt(LocalDateTime.now());
        userRepository.save(user);

        return new UserDataExportDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCpf(),
                user.getPhone(),
                user.getRole() != null ? user.getRole().name() : null,
                user.getCreatedAt(),
                user.getConsentGivenAt(),
                user.getConsentVersion(),
                appointmentSummaries
        );
    }

    private List<AppointmentSummaryDTO> buildAppointmentSummaries(String userId) {
        return patientProfileRepository.findByUserId(userId)
                .map(PatientProfile::getId)
                .map(patientId -> appointmentRepository
                        .findByPatientId(patientId, Pageable.unpaged())
                        .getContent()
                        .stream()
                        .map(this::toSummary)
                        .toList())
                .orElse(List.of());
    }

    private AppointmentSummaryDTO toSummary(Appointment appointment) {
        String professionalName = appointment.getProfessional() != null
                && appointment.getProfessional().getUser() != null
                ? appointment.getProfessional().getUser().getName()
                : null;
        return new AppointmentSummaryDTO(
                appointment.getId(),
                appointment.getStatus() != null ? appointment.getStatus().name() : null,
                appointment.getScheduledAt(),
                professionalName
        );
    }
}
