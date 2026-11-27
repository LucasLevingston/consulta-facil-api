package com.consultafacil.application.scheduler;

import com.consultafacil.application.port.in.user.AnonymizeUserUseCase;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.patient.PatientProfileRepositoryPort;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataRetentionScheduler {

    private static final List<AppointmentStatus> ACTIVE_STATUSES = List.of(
            AppointmentStatus.PENDING,
            AppointmentStatus.CONFIRMED,
            AppointmentStatus.CHECKED_IN,
            AppointmentStatus.IN_PROGRESS
    );

    private final UserRepositoryPort userRepository;
    private final PatientProfileRepositoryPort patientProfileRepository;
    private final AppointmentRepositoryPort appointmentRepository;
    private final AnonymizeUserUseCase anonymizeUser;

    @Scheduled(cron = "0 0 2 1 * *", zone = "America/Sao_Paulo")
    @Transactional
    public void anonymizeInactiveUsers() {
        LocalDateTime cutoff = LocalDateTime.now().minusYears(5);
        List<User> candidates = userRepository.findByCreatedAtBeforeAndDeletedAtIsNull(cutoff);

        log.info("[DataRetention] Found {} user(s) eligible for anonymization (older than 5 years, not deleted)",
                candidates.size());

        for (User user : candidates) {
            try {
                if (!hasActiveAppointments(user)) {
                    log.info("[DataRetention] Anonymizing user={}", user.getId());
                    anonymizeUser.execute(user.getId());
                }
            } catch (Exception e) {
                log.error("[DataRetention] Failed to anonymize user={}: {}", user.getId(), e.getMessage());
            }
        }
    }

    private boolean hasActiveAppointments(User user) {
        return patientProfileRepository.findByUserId(user.getId())
                .map(profile -> ACTIVE_STATUSES.stream()
                        .anyMatch(status -> !appointmentRepository
                                .findByPatientIdAndStatus(profile.getId(), status)
                                .isEmpty()))
                .orElse(false);
    }
}
