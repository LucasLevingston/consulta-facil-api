package com.consultafacil.application.service.analytics;

import com.consultafacil.api.dto.analytics.BreakdownDTO;
import com.consultafacil.api.dto.analytics.KpiDTO;
import com.consultafacil.api.dto.analytics.ReferralAnalyticsDTO;
import com.consultafacil.api.dto.analytics.TimeSeriesDTO;
import com.consultafacil.application.port.in.analytics.ReferralAnalyticsUseCase;
import com.consultafacil.domain.repository.analytics.ReferralAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReferralAnalyticsService implements ReferralAnalyticsUseCase {

    private final ReferralAnalyticsRepository repo;

    @Override
    @Transactional(readOnly = true)
    public ReferralAnalyticsDTO getReferralAnalytics() {
        LocalDateTime since = LocalDateTime.now().minusMonths(12);

        long total = repo.count();
        long availableCommissions = repo.countAvailableCommissions();
        BigDecimal commissionTotal = coalesce(repo.sumCommissions());

        List<KpiDTO> kpis = List.of(
                KpiDTO.count("Total de Indicacoes", total),
                KpiDTO.count("Comissoes Disponiveis", availableCommissions),
                KpiDTO.currency("Total em Comissoes", commissionTotal)
        );

        List<TimeSeriesDTO> referralSeries = repo.countByMonth(since).stream()
                .map(r -> new TimeSeriesDTO(
                        formatMonthLabel((Number) r[0], (Number) r[1]),
                        BigDecimal.valueOf(((Number) r[2]).longValue())
                )).toList();

        List<BreakdownDTO> statusBreakdown = BreakdownDTO.from(repo.groupByStatus());

        return new ReferralAnalyticsDTO(kpis, referralSeries, statusBreakdown);
    }

    private BigDecimal coalesce(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String formatMonthLabel(Number year, Number month) {
        return String.format("%02d/%d", month.intValue(), year.intValue());
    }
}
