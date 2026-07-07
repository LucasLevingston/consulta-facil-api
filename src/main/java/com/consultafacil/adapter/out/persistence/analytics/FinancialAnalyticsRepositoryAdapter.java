package com.consultafacil.adapter.out.persistence.analytics;

import com.consultafacil.domain.enums.BillingPaymentStatus;
import com.consultafacil.domain.port.out.analytics.FinancialAnalyticsPort;
import com.consultafacil.domain.repository.analytics.FinancialAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FinancialAnalyticsRepositoryAdapter implements FinancialAnalyticsPort {

    private final FinancialAnalyticsRepository financialAnalyticsRepository;

    @Override
    public BigDecimal sumAmountByStatus(BillingPaymentStatus status) {
        return financialAnalyticsRepository.sumAmountByStatus(status);
    }

    @Override
    public BigDecimal sumNetAmountByStatus(BillingPaymentStatus status) {
        return financialAnalyticsRepository.sumNetAmountByStatus(status);
    }

    @Override
    public BigDecimal sumSystemFeeByStatus(BillingPaymentStatus status) {
        return financialAnalyticsRepository.sumSystemFeeByStatus(status);
    }

    @Override
    public long countByStatusParam(BillingPaymentStatus status) {
        return financialAnalyticsRepository.countByStatusParam(status);
    }

    @Override
    public List<Object[]> revenueByMonth(BillingPaymentStatus status, LocalDateTime since) {
        return financialAnalyticsRepository.revenueByMonth(status, since);
    }

    @Override
    public List<Object[]> groupByStatus() {
        return financialAnalyticsRepository.groupByStatus();
    }

    @Override
    public List<Object[]> groupByPaymentType(BillingPaymentStatus status) {
        return financialAnalyticsRepository.groupByPaymentType(status);
    }
}
