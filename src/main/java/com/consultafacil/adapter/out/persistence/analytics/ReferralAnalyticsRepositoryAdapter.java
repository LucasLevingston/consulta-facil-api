package com.consultafacil.adapter.out.persistence.analytics;

import com.consultafacil.domain.port.out.analytics.ReferralAnalyticsPort;
import com.consultafacil.domain.repository.analytics.ReferralAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReferralAnalyticsRepositoryAdapter implements ReferralAnalyticsPort {

    private final ReferralAnalyticsRepository referralAnalyticsRepository;

    @Override
    public long count() {
        return referralAnalyticsRepository.count();
    }

    @Override
    public List<Object[]> countByMonth(LocalDateTime since) {
        return referralAnalyticsRepository.countByMonth(since);
    }

    @Override
    public List<Object[]> groupByStatus() {
        return referralAnalyticsRepository.groupByStatus();
    }

    @Override
    public BigDecimal sumCommissions() {
        return referralAnalyticsRepository.sumCommissions();
    }

    @Override
    public long countAvailableCommissions() {
        return referralAnalyticsRepository.countAvailableCommissions();
    }
}
