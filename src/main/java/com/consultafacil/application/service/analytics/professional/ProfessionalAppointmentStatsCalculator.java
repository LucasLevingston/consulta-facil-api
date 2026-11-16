package com.consultafacil.application.service.analytics.professional;

import com.consultafacil.api.dto.analytics.professional.AppointmentStatsDTO;
import com.consultafacil.api.dto.analytics.professional.WeekdayCountDTO;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.enums.AppointmentModality;
import com.consultafacil.domain.enums.AppointmentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProfessionalAppointmentStatsCalculator {

    public AppointmentStatsDTO calculate(List<Appointment> appointments) {
        long total = appointments.size();
        long canceled = count(appointments, AppointmentStatus.CANCELED);
        long pending = count(appointments, AppointmentStatus.PENDING);
        long completed = count(appointments, AppointmentStatus.COMPLETED);
        long confirmed = total - canceled - pending;
        BigDecimal cancellationRate = total == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(canceled).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);

        Map<String, Long> byModality = new LinkedHashMap<>();
        Arrays.stream(AppointmentModality.values()).forEach(m -> byModality.put(m.name(), 0L));
        Map<DayOfWeek, Long> byWeekday = new LinkedHashMap<>();
        Arrays.stream(DayOfWeek.values()).forEach(d -> byWeekday.put(d, 0L));

        for (Appointment a : appointments) {
            if (a.getModality() != null) byModality.merge(a.getModality().name(), 1L, Long::sum);
            if (a.getStatus() != AppointmentStatus.CANCELED && a.getScheduledAt() != null) {
                byWeekday.merge(a.getScheduledAt().getDayOfWeek(), 1L, Long::sum);
            }
        }

        List<WeekdayCountDTO> weekdays = byWeekday.entrySet().stream()
                .map(e -> new WeekdayCountDTO(e.getKey().name(), e.getValue()))
                .toList();
        return new AppointmentStatsDTO(total, confirmed, completed, canceled, pending, cancellationRate,
                byModality, weekdays);
    }

    private long count(List<Appointment> appointments, AppointmentStatus status) {
        return appointments.stream().filter(a -> a.getStatus() == status).count();
    }
}
