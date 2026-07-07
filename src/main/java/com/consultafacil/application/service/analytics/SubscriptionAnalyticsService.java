package com.consultafacil.application.service.analytics;

import com.consultafacil.api.dto.analytics.BreakdownDTO;
import com.consultafacil.api.dto.analytics.KpiDTO;
import com.consultafacil.api.dto.analytics.SubscriptionAnalyticsDTO;
import com.consultafacil.application.port.in.analytics.SubscriptionAnalyticsUseCase;
import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.port.out.analytics.SubscriptionAnalyticsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionAnalyticsService implements SubscriptionAnalyticsUseCase {

    private final SubscriptionAnalyticsPort repo;

    @Override
    @Transactional(readOnly = true)
    public SubscriptionAnalyticsDTO getSubscriptionAnalytics() {
        long total = repo.count();
        long active = repo.countByStatus(SubscriptionStatus.ACTIVE);
        long cancelled = repo.countByStatus(SubscriptionStatus.CANCELLED);
        long expired = repo.countByStatus(SubscriptionStatus.EXPIRED);
        long pending = repo.countByStatus(SubscriptionStatus.PENDING);

        List<KpiDTO> kpis = List.of(
                KpiDTO.count("Total de Assinaturas", total),
                KpiDTO.count("Ativas", active),
                KpiDTO.count("Canceladas", cancelled),
                KpiDTO.count("Expiradas", expired),
                KpiDTO.count("Pendentes", pending)
        );

        List<BreakdownDTO> statusBreakdown = BreakdownDTO.from(repo.groupByStatus());
        List<BreakdownDTO> planBreakdown = BreakdownDTO.from(repo.groupByPlan());

        return new SubscriptionAnalyticsDTO(kpis, statusBreakdown, planBreakdown);
    }
}
