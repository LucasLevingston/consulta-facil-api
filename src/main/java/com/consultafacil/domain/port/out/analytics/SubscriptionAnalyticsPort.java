package com.consultafacil.domain.port.out.analytics;

import com.consultafacil.domain.enums.SubscriptionStatus;

import java.util.List;

public interface SubscriptionAnalyticsPort {

    long count();

    long countByStatus(SubscriptionStatus status);

    List<Object[]> groupByStatus();

    List<Object[]> groupByPlan();
}
