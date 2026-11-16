package com.consultafacil.application.service.analytics;

import com.consultafacil.api.dto.analytics.KpiDTO;
import com.consultafacil.api.dto.analytics.ReferralAnalyticsDTO;
import com.consultafacil.domain.port.out.analytics.ReferralAnalyticsPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferralAnalyticsServiceTest {

    @Mock ReferralAnalyticsPort repo;

    ReferralAnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new ReferralAnalyticsService(repo);
    }

    @Test
    void execute_buildsKpisFromReferralCounts() {
        when(repo.count()).thenReturn(7L);
        when(repo.countAvailableCommissions()).thenReturn(3L);
        when(repo.sumCommissions()).thenReturn(new BigDecimal("245.90"));
        when(repo.countByMonth(any())).thenReturn(List.of());
        when(repo.groupByStatus()).thenReturn(List.of());

        ReferralAnalyticsDTO result = service.getReferralAnalytics();

        assertThat(result.kpis()).extracting(KpiDTO::label)
                .containsExactly("Total de Indicacoes", "Comissoes Disponiveis", "Total em Comissoes");
        assertThat(result.kpis().get(0).value()).isEqualByComparingTo("7");
        assertThat(result.kpis().get(1).value()).isEqualByComparingTo("3");
        assertThat(result.kpis().get(2).value()).isEqualByComparingTo("245.90");
        assertThat(result.kpis().get(2).unit()).isEqualTo("BRL");
    }

    @Test
    void execute_treatsNullCommissionSumAsZero() {
        when(repo.count()).thenReturn(0L);
        when(repo.countAvailableCommissions()).thenReturn(0L);
        when(repo.sumCommissions()).thenReturn(null);
        when(repo.countByMonth(any())).thenReturn(List.of());
        when(repo.groupByStatus()).thenReturn(List.of());

        ReferralAnalyticsDTO result = service.getReferralAnalytics();

        assertThat(result.kpis().get(2).value()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void execute_mapsMonthlySeriesAndStatusBreakdown() {
        when(repo.count()).thenReturn(2L);
        when(repo.countAvailableCommissions()).thenReturn(0L);
        when(repo.sumCommissions()).thenReturn(BigDecimal.TEN);
        when(repo.countByMonth(any())).thenReturn(List.<Object[]>of(new Object[]{2025, 12, 2L}));
        when(repo.groupByStatus()).thenReturn(List.<Object[]>of(
                new Object[]{"PENDING", 1L},
                new Object[]{"CONVERTED", 1L}));

        ReferralAnalyticsDTO result = service.getReferralAnalytics();

        assertThat(result.referralSeries()).singleElement().satisfies(point -> {
            assertThat(point.label()).isEqualTo("12/2025");
            assertThat(point.value()).isEqualByComparingTo("2");
        });
        assertThat(result.statusBreakdown()).hasSize(2)
                .allSatisfy(b -> assertThat(b.percentage()).isEqualByComparingTo("50.0"));
    }
}
