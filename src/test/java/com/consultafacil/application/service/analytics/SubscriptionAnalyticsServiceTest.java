package com.consultafacil.application.service.analytics;

import com.consultafacil.api.dto.analytics.KpiDTO;
import com.consultafacil.api.dto.analytics.SubscriptionAnalyticsDTO;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.port.out.analytics.SubscriptionAnalyticsPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionAnalyticsServiceTest {

    @Mock SubscriptionAnalyticsPort repo;

    SubscriptionAnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new SubscriptionAnalyticsService(repo);
        when(repo.count()).thenReturn(10L);
        when(repo.countByStatus(SubscriptionStatus.ACTIVE)).thenReturn(6L);
        when(repo.countByStatus(SubscriptionStatus.CANCELLED)).thenReturn(2L);
        when(repo.countByStatus(SubscriptionStatus.EXPIRED)).thenReturn(1L);
        when(repo.countByStatus(SubscriptionStatus.PENDING)).thenReturn(1L);
    }

    @Test
    void execute_buildsKpisPerSubscriptionStatus() {
        when(repo.groupByStatus()).thenReturn(List.of());
        when(repo.groupByPlan()).thenReturn(List.of());

        SubscriptionAnalyticsDTO result = service.getSubscriptionAnalytics();

        assertThat(result.kpis()).extracting(KpiDTO::label)
                .containsExactly("Total de Assinaturas", "Ativas", "Canceladas", "Expiradas", "Pendentes");
        assertThat(result.kpis()).extracting(kpi -> kpi.value().longValue())
                .containsExactly(10L, 6L, 2L, 1L, 1L);
        assertThat(result.kpis()).allSatisfy(kpi -> assertThat(kpi.unit()).isEqualTo("count"));
    }

    @Test
    void execute_buildsStatusAndPlanBreakdowns() {
        when(repo.groupByStatus()).thenReturn(List.<Object[]>of(
                new Object[]{"ACTIVE", 6L},
                new Object[]{"CANCELLED", 4L}));
        when(repo.groupByPlan()).thenReturn(List.<Object[]>of(new Object[]{"Profissional", 10L}));

        SubscriptionAnalyticsDTO result = service.getSubscriptionAnalytics();

        assertThat(result.statusBreakdown()).extracting(b -> b.percentage().toPlainString())
                .containsExactly("60.0", "40.0");
        assertThat(result.planBreakdown()).singleElement().satisfies(b -> {
            assertThat(b.label()).isEqualTo("Profissional");
            assertThat(b.count()).isEqualTo(10L);
        });
    }
}
