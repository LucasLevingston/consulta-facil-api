package com.consultafacil.application.service.analytics.professional;

import com.consultafacil.api.dto.analytics.professional.ProfessionalAnalyticsDTO;
import com.consultafacil.application.port.in.analytics.GetProfessionalAnalyticsUseCase;
import com.consultafacil.core.exception.BadRequestException;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.entity.ProfessionalProfile;
import com.consultafacil.domain.enums.AnalyticsPeriod;
import com.consultafacil.domain.port.out.appointment.AppointmentRepositoryPort;
import com.consultafacil.domain.port.out.professional.profile.ProfessionalProfileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetProfessionalAnalyticsService implements GetProfessionalAnalyticsUseCase {

    private final ProfessionalProfileRepositoryPort professionalProfileRepository;
    private final AppointmentRepositoryPort appointmentRepository;
    private final ProfessionalRevenueCalculator revenueCalculator;
    private final ProfessionalAppointmentStatsCalculator appointmentStatsCalculator;
    private final ProfessionalRatingStatsCalculator ratingStatsCalculator;
    private final ProfessionalTopServicesCalculator topServicesCalculator;

    @Override
    @Transactional(readOnly = true)
    public ProfessionalAnalyticsDTO execute(String userId, String periodCode) {
        AnalyticsPeriod period = AnalyticsPeriod.fromCode(periodCode)
                .orElseThrow(() -> new BadRequestException("Período inválido: " + periodCode + ". Use 30d, 90d ou 12m."));
        ProfessionalProfile profile = professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("ProfessionalProfile", userId));

        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = period.startFrom(to);
        List<Appointment> appointments =
                appointmentRepository.findByProfessionalIdAndScheduledAtBetween(profile.getId(), from, to);

        return new ProfessionalAnalyticsDTO(
                period.getCode(),
                from,
                to,
                revenueCalculator.calculate(appointments, YearMonth.from(from), YearMonth.from(to)),
                appointmentStatsCalculator.calculate(appointments),
                ratingStatsCalculator.calculate(appointments),
                topServicesCalculator.calculate(appointments));
    }
}
