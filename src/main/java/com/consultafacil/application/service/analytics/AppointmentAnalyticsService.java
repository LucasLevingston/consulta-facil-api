package com.consultafacil.application.service.analytics;

import com.consultafacil.api.dto.analytics.AppointmentAnalyticsDTO;
import com.consultafacil.api.dto.analytics.BreakdownDTO;
import com.consultafacil.api.dto.analytics.KpiDTO;
import com.consultafacil.api.dto.analytics.TimeSeriesDTO;
import com.consultafacil.application.port.in.analytics.AppointmentAnalyticsUseCase;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.repository.analytics.AppointmentAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentAnalyticsService implements AppointmentAnalyticsUseCase {

    private final AppointmentAnalyticsRepository repo;

    @Override
    @Transactional(readOnly = true)
    public AppointmentAnalyticsDTO getAppointmentAnalytics() {
        LocalDateTime since = LocalDateTime.now().minusMonths(12);

        long total = repo.count();
        long completed = repo.countByStatus(AppointmentStatus.COMPLETED);
        long canceled = repo.countByStatus(AppointmentStatus.CANCELED);
        long pending = repo.countByStatus(AppointmentStatus.PENDING);

        BigDecimal completionRate = total == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(completed * 100.0 / total).setScale(1, RoundingMode.HALF_UP);

        List<KpiDTO> kpis = List.of(
                KpiDTO.count("Total de Consultas", total),
                KpiDTO.count("Concluidas", completed),
                KpiDTO.count("Canceladas", canceled),
                KpiDTO.count("Pendentes", pending),
                new KpiDTO("Taxa de Conclusao", completionRate, "%")
        );

        List<TimeSeriesDTO> appointmentSeries = repo.countByMonth(since).stream()
                .map(r -> new TimeSeriesDTO(
                        formatMonthLabel((Number) r[0], (Number) r[1]),
                        BigDecimal.valueOf(((Number) r[2]).longValue())
                )).toList();

        List<BreakdownDTO> statusBreakdown = BreakdownDTO.from(repo.groupByStatus());
        List<BreakdownDTO> modalityBreakdown = BreakdownDTO.from(repo.groupByModality());

        return new AppointmentAnalyticsDTO(kpis, appointmentSeries, statusBreakdown, modalityBreakdown);
    }

    private String formatMonthLabel(Number year, Number month) {
        return String.format("%02d/%d", month.intValue(), year.intValue());
    }
}
