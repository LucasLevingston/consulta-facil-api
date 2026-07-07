package com.consultafacil.domain.port.out.analytics;

import com.consultafacil.domain.enums.BillingPaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface FinancialAnalyticsPort {

    BigDecimal sumAmountByStatus(BillingPaymentStatus status);

    BigDecimal sumNetAmountByStatus(BillingPaymentStatus status);

    BigDecimal sumSystemFeeByStatus(BillingPaymentStatus status);

    long countByStatusParam(BillingPaymentStatus status);

    List<Object[]> revenueByMonth(BillingPaymentStatus status, LocalDateTime since);

    List<Object[]> groupByStatus();

    List<Object[]> groupByPaymentType(BillingPaymentStatus status);
}
