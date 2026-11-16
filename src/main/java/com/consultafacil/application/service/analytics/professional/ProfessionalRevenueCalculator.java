package com.consultafacil.application.service.analytics.professional;

import com.consultafacil.api.dto.analytics.professional.MonthlyRevenueDTO;
import com.consultafacil.api.dto.analytics.professional.RevenueStatsDTO;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.enums.AppointmentPaymentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Revenue = paymentAmount of PAID appointments, bucketed by the month of the appointment. */
@Component
public class ProfessionalRevenueCalculator {

    public RevenueStatsDTO calculate(List<Appointment> appointments, YearMonth firstMonth, YearMonth lastMonth) {
        Map<YearMonth, BigDecimal> byMonth = new LinkedHashMap<>();
        for (YearMonth m = firstMonth; !m.isAfter(lastMonth); m = m.plusMonths(1)) {
            byMonth.put(m, BigDecimal.ZERO);
        }

        BigDecimal total = BigDecimal.ZERO;
        long paid = 0;
        for (Appointment a : appointments) {
            if (a.getPaymentStatus() != AppointmentPaymentStatus.PAID || a.getPaymentAmount() == null) continue;
            paid++;
            total = total.add(a.getPaymentAmount());
            byMonth.computeIfPresent(YearMonth.from(a.getScheduledAt()), (m, sum) -> sum.add(a.getPaymentAmount()));
        }

        BigDecimal avg = paid == 0 ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(paid), 2, RoundingMode.HALF_UP);
        List<MonthlyRevenueDTO> series = new ArrayList<>();
        byMonth.forEach((month, amount) -> series.add(new MonthlyRevenueDTO(month.toString(), amount)));
        return new RevenueStatsDTO(total, avg, paid, series);
    }
}
