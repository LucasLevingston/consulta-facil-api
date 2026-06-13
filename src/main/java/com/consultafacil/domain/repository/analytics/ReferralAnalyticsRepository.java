package com.consultafacil.domain.repository.analytics;

import com.consultafacil.domain.entity.Referral;
import com.consultafacil.domain.entity.ReferralCommission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ReferralAnalyticsRepository extends JpaRepository<Referral, String> {

    @Query("SELECT YEAR(r.createdAt), MONTH(r.createdAt), COUNT(r) FROM Referral r " +
           "WHERE r.createdAt >= :since " +
           "GROUP BY YEAR(r.createdAt), MONTH(r.createdAt) " +
           "ORDER BY YEAR(r.createdAt), MONTH(r.createdAt)")
    List<Object[]> countByMonth(@Param("since") LocalDateTime since);

    @Query("SELECT r.status, COUNT(r) FROM Referral r GROUP BY r.status")
    List<Object[]> groupByStatus();

    @Query("SELECT SUM(rc.amount) FROM ReferralCommission rc WHERE rc.status <> 'CANCELED'")
    BigDecimal sumCommissions();

    @Query("SELECT COUNT(rc) FROM ReferralCommission rc WHERE rc.status = 'AVAILABLE'")
    long countAvailableCommissions();
}
