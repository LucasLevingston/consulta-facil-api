package com.consultafacil.application.service.analytics;

import com.consultafacil.api.dto.analytics.BreakdownDTO;
import com.consultafacil.api.dto.analytics.KpiDTO;
import com.consultafacil.api.dto.analytics.TimeSeriesDTO;
import com.consultafacil.api.dto.analytics.UserAnalyticsDTO;
import com.consultafacil.application.port.in.analytics.UserAnalyticsUseCase;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.analytics.UserAnalyticsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAnalyticsService implements UserAnalyticsUseCase {

    private final UserAnalyticsPort repo;

    @Override
    @Transactional(readOnly = true)
    public UserAnalyticsDTO getUserAnalytics() {
        LocalDateTime since = LocalDateTime.now().minusMonths(12);
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        long total = repo.count();
        long patients = repo.countByRole(UserRole.PATIENT);
        long professionals = repo.countByRole(UserRole.PROFESSIONAL);
        long newThisMonth = repo.countNewSince(startOfMonth);

        List<KpiDTO> kpis = List.of(
                KpiDTO.count("Total de Usuarios", total),
                KpiDTO.count("Pacientes", patients),
                KpiDTO.count("Profissionais", professionals),
                KpiDTO.count("Novos este Mes", newThisMonth)
        );

        List<TimeSeriesDTO> growthSeries = repo.growthByMonth(since).stream()
                .map(r -> new TimeSeriesDTO(
                        formatMonthLabel((Number) r[0], (Number) r[1]),
                        BigDecimal.valueOf(((Number) r[2]).longValue())
                )).toList();

        List<BreakdownDTO> roleBreakdown = BreakdownDTO.from(repo.groupByRole());

        return new UserAnalyticsDTO(kpis, growthSeries, roleBreakdown);
    }

    private String formatMonthLabel(Number year, Number month) {
        return String.format("%02d/%d", month.intValue(), year.intValue());
    }
}
