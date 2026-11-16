package com.consultafacil.application.service.analytics.professional;

import com.consultafacil.api.dto.analytics.professional.MonthlyRevenueDTO;
import com.consultafacil.api.dto.analytics.professional.RevenueStatsDTO;
import com.consultafacil.domain.entity.Appointment;
import com.consultafacil.domain.enums.AppointmentPaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProfessionalRevenueCalculatorTest {

    private final ProfessionalRevenueCalculator calculator = new ProfessionalRevenueCalculator();

    private Appointment appointment(LocalDateTime at, AppointmentPaymentStatus status, String amount) {
        return Appointment.builder()
                .scheduledAt(at)
                .paymentStatus(status)
                .paymentAmount(amount == null ? null : new BigDecimal(amount))
                .build();
    }

    @Test
    void sumsOnlyPaidAppointmentsAndAveragesPerPaidAppointment() {
        List<Appointment> appointments = List.of(
                appointment(LocalDateTime.of(2026, 10, 3, 9, 0), AppointmentPaymentStatus.PAID, "200.00"),
                appointment(LocalDateTime.of(2026, 10, 9, 9, 0), AppointmentPaymentStatus.PAID, "150.00"),
                appointment(LocalDateTime.of(2026, 11, 1, 9, 0), AppointmentPaymentStatus.PAID, "100.00"),
                appointment(LocalDateTime.of(2026, 11, 2, 9, 0), AppointmentPaymentStatus.REFUNDED, "300.00"),
                appointment(LocalDateTime.of(2026, 11, 3, 9, 0), AppointmentPaymentStatus.UNPAID, "90.00"),
                appointment(LocalDateTime.of(2026, 11, 4, 9, 0), AppointmentPaymentStatus.PAID, null));

        RevenueStatsDTO result = calculator.calculate(appointments, YearMonth.of(2026, 10), YearMonth.of(2026, 11));

        assertThat(result.total()).isEqualByComparingTo("450.00");
        assertThat(result.paidAppointments()).isEqualTo(3);
        assertThat(result.avgPerAppointment()).isEqualByComparingTo("150.00");
        assertThat(result.byMonth()).extracting(MonthlyRevenueDTO::month).containsExactly("2026-10", "2026-11");
        assertThat(result.byMonth().get(0).amount()).isEqualByComparingTo("350.00");
        assertThat(result.byMonth().get(1).amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void zeroFillsEveryMonthOfTheWindow() {
        RevenueStatsDTO result = calculator.calculate(
                List.of(appointment(LocalDateTime.of(2026, 3, 15, 9, 0), AppointmentPaymentStatus.PAID, "80.00")),
                YearMonth.of(2025, 12), YearMonth.of(2026, 3));

        assertThat(result.byMonth()).extracting(MonthlyRevenueDTO::month)
                .containsExactly("2025-12", "2026-01", "2026-02", "2026-03");
        assertThat(result.byMonth()).extracting(m -> m.amount().toPlainString())
                .containsExactly("0", "0", "0", "80.00");
    }

    @Test
    void roundsAverageToCents() {
        RevenueStatsDTO result = calculator.calculate(List.of(
                appointment(LocalDateTime.of(2026, 1, 1, 9, 0), AppointmentPaymentStatus.PAID, "100.00"),
                appointment(LocalDateTime.of(2026, 1, 2, 9, 0), AppointmentPaymentStatus.PAID, "100.00"),
                appointment(LocalDateTime.of(2026, 1, 3, 9, 0), AppointmentPaymentStatus.PAID, "100.01")),
                YearMonth.of(2026, 1), YearMonth.of(2026, 1));

        assertThat(result.avgPerAppointment()).isEqualByComparingTo("100.00");
    }

    @Test
    void noPaidAppointmentsYieldsZeroes() {
        RevenueStatsDTO result = calculator.calculate(List.of(), YearMonth.of(2026, 11), YearMonth.of(2026, 11));

        assertThat(result.total()).isEqualByComparingTo("0");
        assertThat(result.avgPerAppointment()).isEqualByComparingTo("0");
        assertThat(result.paidAppointments()).isZero();
        assertThat(result.byMonth()).singleElement().satisfies(m -> assertThat(m.month()).isEqualTo("2026-11"));
    }
}
