package com.consultafacil.application.service.analytics;

import com.consultafacil.api.dto.analytics.KpiDTO;
import com.consultafacil.api.dto.analytics.UserAnalyticsDTO;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.analytics.UserAnalyticsPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAnalyticsServiceTest {

    @Mock UserAnalyticsPort repo;

    UserAnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new UserAnalyticsService(repo);
        when(repo.count()).thenReturn(20L);
        when(repo.countByRole(UserRole.PATIENT)).thenReturn(15L);
        when(repo.countByRole(UserRole.PROFESSIONAL)).thenReturn(4L);
        when(repo.countNewSince(any())).thenReturn(3L);
        when(repo.growthByMonth(any())).thenReturn(List.of());
        when(repo.groupByRole()).thenReturn(List.of());
    }

    @Test
    void execute_buildsKpisPerRoleAndNewUsersThisMonth() {
        UserAnalyticsDTO result = service.getUserAnalytics();

        assertThat(result.kpis()).extracting(KpiDTO::label)
                .containsExactly("Total de Usuarios", "Pacientes", "Profissionais", "Novos este Mes");
        assertThat(result.kpis()).extracting(kpi -> kpi.value().longValue())
                .containsExactly(20L, 15L, 4L, 3L);
    }

    @Test
    void execute_countsNewUsersFromTheStartOfTheCurrentMonth() {
        service.getUserAnalytics();

        ArgumentCaptor<LocalDateTime> since = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(repo).countNewSince(since.capture());
        LocalDateTime now = LocalDateTime.now();
        assertThat(since.getValue().getDayOfMonth()).isEqualTo(1);
        assertThat(since.getValue().getMonth()).isEqualTo(now.getMonth());
        assertThat(since.getValue().getHour()).isZero();
        assertThat(since.getValue().getMinute()).isZero();
    }

    @Test
    void execute_mapsGrowthSeriesAndRoleBreakdown() {
        when(repo.growthByMonth(any())).thenReturn(List.<Object[]>of(
                new Object[]{2026, 9, 5L},
                new Object[]{2026, 10, 7L}));
        when(repo.groupByRole()).thenReturn(List.<Object[]>of(
                new Object[]{UserRole.PATIENT, 15L},
                new Object[]{UserRole.PROFESSIONAL, 5L}));

        UserAnalyticsDTO result = service.getUserAnalytics();

        assertThat(result.growthSeries()).extracting(p -> p.label()).containsExactly("09/2026", "10/2026");
        assertThat(result.growthSeries().get(1).value()).isEqualByComparingTo("7");
        assertThat(result.roleBreakdown()).extracting(b -> b.label()).containsExactly("PATIENT", "PROFESSIONAL");
        assertThat(result.roleBreakdown().get(0).percentage()).isEqualByComparingTo("75.0");
    }
}
