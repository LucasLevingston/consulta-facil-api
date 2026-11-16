package com.consultafacil.application.service.analytics.professional;

import com.consultafacil.api.dto.analytics.professional.AppointmentStatsDTO;
import com.consultafacil.api.dto.analytics.professional.WeekdayCountDTO;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.enums.AppointmentModality;
import com.consultafacil.domain.enums.AppointmentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProfessionalAppointmentStatsCalculatorTest {

    private final ProfessionalAppointmentStatsCalculator calculator = new ProfessionalAppointmentStatsCalculator();

    // 2026-11-16 is a Monday
    private Appointment appointment(AppointmentStatus status, AppointmentModality modality, int dayOfMonth) {
        return Appointment.builder()
                .status(status)
                .modality(modality)
                .scheduledAt(LocalDateTime.of(2026, 11, dayOfMonth, 10, 0))
                .build();
    }

    @Test
    void countsStatusesAndCancellationRate() {
        AppointmentStatsDTO stats = calculator.calculate(List.of(
                appointment(AppointmentStatus.COMPLETED, AppointmentModality.IN_PERSON, 16),
                appointment(AppointmentStatus.COMPLETED, AppointmentModality.ONLINE, 16),
                appointment(AppointmentStatus.CONFIRMED, AppointmentModality.ONLINE, 17),
                appointment(AppointmentStatus.IN_PROGRESS, AppointmentModality.IN_PERSON, 18),
                appointment(AppointmentStatus.PENDING, AppointmentModality.IN_PERSON, 19),
                appointment(AppointmentStatus.CANCELED, AppointmentModality.IN_PERSON, 20),
                appointment(AppointmentStatus.CANCELED, AppointmentModality.ONLINE, 21),
                appointment(AppointmentStatus.CHECKED_IN, AppointmentModality.IN_PERSON, 22)));

        assertThat(stats.total()).isEqualTo(8);
        assertThat(stats.canceled()).isEqualTo(2);
        assertThat(stats.pending()).isEqualTo(1);
        assertThat(stats.completed()).isEqualTo(2);
        assertThat(stats.confirmed()).isEqualTo(5);
        assertThat(stats.cancellationRate()).isEqualByComparingTo("0.25");
    }

    @Test
    void splitsByModalityIncludingCanceled() {
        AppointmentStatsDTO stats = calculator.calculate(List.of(
                appointment(AppointmentStatus.CONFIRMED, AppointmentModality.ONLINE, 16),
                appointment(AppointmentStatus.CANCELED, AppointmentModality.ONLINE, 16),
                appointment(AppointmentStatus.CONFIRMED, AppointmentModality.IN_PERSON, 16)));

        assertThat(stats.byModality()).containsExactly(
                org.assertj.core.api.Assertions.entry("IN_PERSON", 1L),
                org.assertj.core.api.Assertions.entry("ONLINE", 2L));
    }

    @Test
    void countsWeekdaysMondayToSundayIgnoringCanceled() {
        AppointmentStatsDTO stats = calculator.calculate(List.of(
                appointment(AppointmentStatus.CONFIRMED, AppointmentModality.IN_PERSON, 16), // MONDAY
                appointment(AppointmentStatus.COMPLETED, AppointmentModality.IN_PERSON, 23), // MONDAY
                appointment(AppointmentStatus.CANCELED, AppointmentModality.IN_PERSON, 24), // TUESDAY (ignored)
                appointment(AppointmentStatus.PENDING, AppointmentModality.IN_PERSON, 22))); // SUNDAY

        assertThat(stats.byWeekday()).extracting(WeekdayCountDTO::day).containsExactly(
                "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");
        assertThat(stats.byWeekday()).extracting(WeekdayCountDTO::count).containsExactly(2L, 0L, 0L, 0L, 0L, 0L, 1L);
    }

    @Test
    void emptyPeriodHasZeroRateAndZeroFilledBreakdowns() {
        AppointmentStatsDTO stats = calculator.calculate(List.of());

        assertThat(stats.total()).isZero();
        assertThat(stats.cancellationRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(stats.byModality()).containsOnlyKeys("IN_PERSON", "ONLINE").allSatisfy((k, v) -> assertThat(v).isZero());
        assertThat(stats.byWeekday()).hasSize(7).allSatisfy(d -> assertThat(d.count()).isZero());
    }
}
