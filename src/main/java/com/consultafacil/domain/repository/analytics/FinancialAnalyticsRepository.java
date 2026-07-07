package com.consultafacil.domain.repository.analytics;

import com.consultafacil.domain.entity.BillingPayment;
import com.consultafacil.domain.enums.BillingPaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface FinancialAnalyticsRepository extends JpaRepository<BillingPayment, String> {

    @Query("SELECT SUM(p.amount) FROM BillingPayment p WHERE p.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") BillingPaymentStatus status);

    @Query("SELECT SUM(p.netAmount) FROM BillingPayment p WHERE p.status = :status")
    BigDecimal sumNetAmountByStatus(@Param("status") BillingPaymentStatus status);

    @Query("SELECT SUM(p.systemFee) FROM BillingPayment p WHERE p.status = :status")
    BigDecimal sumSystemFeeByStatus(@Param("status") BillingPaymentStatus status);

    @Query("SELECT COUNT(p) FROM BillingPayment p WHERE p.status = :status")
    long countByStatusParam(@Param("status") BillingPaymentStatus status);

    @Query("SELECT YEAR(p.paidAt), MONTH(p.paidAt), SUM(p.amount) FROM BillingPayment p " +
           "WHERE p.status = :status AND p.paidAt >= :since " +
           "GROUP BY YEAR(p.paidAt), MONTH(p.paidAt) " +
           "ORDER BY YEAR(p.paidAt), MONTH(p.paidAt)")
    List<Object[]> revenueByMonth(@Param("status") BillingPaymentStatus status, @Param("since") LocalDateTime since);

    @Query("SELECT p.status, COUNT(p) FROM BillingPayment p GROUP BY p.status")
    List<Object[]> groupByStatus();

    @Query("SELECT p.paymentType, COUNT(p) FROM BillingPayment p WHERE p.status = :status GROUP BY p.paymentType")
    List<Object[]> groupByPaymentType(@Param("status") BillingPaymentStatus status);
}
