package com.consultafacil.adapter.out.persistence.analytics;

import com.consultafacil.domain.enums.SubscriptionStatus;
import com.consultafacil.domain.port.out.analytics.SubscriptionAnalyticsPort;
import com.consultafacil.domain.repository.analytics.SubscriptionAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SubscriptionAnalyticsRepositoryAdapter implements SubscriptionAnalyticsPort {

    private final SubscriptionAnalyticsRepository subscriptionAnalyticsRepository;

    @Override
    public long count() {
        return subscriptionAnalyticsRepository.count();
    }

    @Override
    public long countByStatus(SubscriptionStatus status) {
        return subscriptionAnalyticsRepository.countByStatus(status);
    }

    @Override
    public List<Object[]> groupByStatus() {
        return subscriptionAnalyticsRepository.groupByStatus();
    }

    @Override
    public List<Object[]> groupByPlan() {
        return subscriptionAnalyticsRepository.groupByPlan();
    }
}
