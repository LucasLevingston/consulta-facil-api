package com.consultafacil.application.service.analytics;

import com.consultafacil.api.dto.analytics.AppointmentAnalyticsDTO;
import com.consultafacil.api.dto.analytics.KpiDTO;
import com.consultafacil.domain.enums.AppointmentStatus;
import com.consultafacil.domain.port.out.analytics.AppointmentAnalyticsPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentAnalyticsServiceTest {

    @Mock AppointmentAnalyticsPort repo;

    AppointmentAnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new AppointmentAnalyticsService(repo);
    }

    private void stubCounts(long total, long completed, long canceled, long pending) {
        when(repo.count()).thenReturn(total);
        when(repo.countByStatus(AppointmentStatus.COMPLETED)).thenReturn(completed);
        when(repo.countByStatus(AppointmentStatus.CANCELED)).thenReturn(canceled);
        when(repo.countByStatus(AppointmentStatus.PENDING)).thenReturn(pending);
    }

    @Test
    void execute_buildsKpisWithCompletionRateRoundedToOneDecimal() {
        stubCounts(3, 2, 1, 0);
        when(repo.countByMonth(any())).thenReturn(List.of());
        when(repo.groupByStatus()).thenReturn(List.of());
        when(repo.groupByModality()).thenReturn(List.of());

        AppointmentAnalyticsDTO result = service.getAppointmentAnalytics();

        assertThat(result.kpis()).extracting(KpiDTO::label).containsExactly(
                "Total de Consultas", "Concluidas", "Canceladas", "Pendentes", "Taxa de Conclusao");
        assertThat(result.kpis().get(0).value()).isEqualByComparingTo("3");
        assertThat(result.kpis().get(1).value()).isEqualByComparingTo("2");
        KpiDTO rate = result.kpis().get(4);
        assertThat(rate.value()).isEqualByComparingTo("66.7");
        assertThat(rate.unit()).isEqualTo("%");
    }

    @Test
    void execute_returnsZeroCompletionRateWhenThereAreNoAppointments() {
        stubCounts(0, 0, 0, 0);
        when(repo.countByMonth(any())).thenReturn(List.of());
        when(repo.groupByStatus()).thenReturn(List.of());
        when(repo.groupByModality()).thenReturn(List.of());

        AppointmentAnalyticsDTO result = service.getAppointmentAnalytics();

        assertThat(result.kpis().get(4).value()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void execute_mapsMonthlySeriesWithZeroPaddedLabels() {
        stubCounts(5, 5, 0, 0);
        when(repo.countByMonth(any())).thenReturn(List.<Object[]>of(
                new Object[]{2026, 3, 4L},
                new Object[]{2026, 11, 1L}));
        when(repo.groupByStatus()).thenReturn(List.of());
        when(repo.groupByModality()).thenReturn(List.of());

        AppointmentAnalyticsDTO result = service.getAppointmentAnalytics();

        assertThat(result.appointmentSeries()).hasSize(2);
        assertThat(result.appointmentSeries().get(0).label()).isEqualTo("03/2026");
        assertThat(result.appointmentSeries().get(0).value()).isEqualByComparingTo("4");
        assertThat(result.appointmentSeries().get(1).label()).isEqualTo("11/2026");
    }

    @Test
    void execute_queriesTheLastTwelveMonths() {
        stubCounts(0, 0, 0, 0);
        when(repo.countByMonth(any())).thenReturn(List.of());
        when(repo.groupByStatus()).thenReturn(List.of());
        when(repo.groupByModality()).thenReturn(List.of());

        service.getAppointmentAnalytics();

        ArgumentCaptor<LocalDateTime> since = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(repo).countByMonth(since.capture());
        assertThat(since.getValue()).isCloseTo(LocalDateTime.now().minusMonths(12), within(1, java.time.temporal.ChronoUnit.MINUTES));
    }

    @Test
    void execute_buildsStatusAndModalityBreakdowns() {
        stubCounts(4, 3, 1, 0);
        when(repo.countByMonth(any())).thenReturn(List.of());
        when(repo.groupByStatus()).thenReturn(List.<Object[]>of(
                new Object[]{"COMPLETED", 3L},
                new Object[]{"CANCELED", 1L}));
        when(repo.groupByModality()).thenReturn(List.<Object[]>of(
                new Object[]{"ONLINE", 1L},
                new Object[]{null, 1L}));

        AppointmentAnalyticsDTO result = service.getAppointmentAnalytics();

        assertThat(result.statusBreakdown()).hasSize(2);
        assertThat(result.statusBreakdown().get(0).label()).isEqualTo("COMPLETED");
        assertThat(result.statusBreakdown().get(0).percentage()).isEqualByComparingTo("75.0");
        assertThat(result.modalityBreakdown().get(1).label()).isEqualTo("UNKNOWN");
        assertThat(result.modalityBreakdown().get(1).percentage()).isEqualByComparingTo("50.0");
    }
}
