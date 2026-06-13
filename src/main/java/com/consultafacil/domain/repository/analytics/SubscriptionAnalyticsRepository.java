package com.consultafacil.domain.repository.analytics;

import com.consultafacil.domain.entity.Subscription;
import com.consultafacil.domain.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubscriptionAnalyticsRepository extends JpaRepository<Subscription, String> {

    long countByStatus(SubscriptionStatus status);

    @Query("SELECT s.status, COUNT(s) FROM Subscription s GROUP BY s.status")
    List<Object[]> groupByStatus();

    @Query("SELECT s.planId, COUNT(s) FROM Subscription s GROUP BY s.planId")
    List<Object[]> groupByPlan();
}
