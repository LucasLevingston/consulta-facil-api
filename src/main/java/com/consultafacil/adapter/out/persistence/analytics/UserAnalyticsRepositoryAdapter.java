package com.consultafacil.adapter.out.persistence.analytics;

import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.analytics.UserAnalyticsPort;
import com.consultafacil.domain.repository.analytics.UserAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserAnalyticsRepositoryAdapter implements UserAnalyticsPort {

    private final UserAnalyticsRepository userAnalyticsRepository;

    @Override
    public long count() {
        return userAnalyticsRepository.count();
    }

    @Override
    public long countByRole(UserRole role) {
        return userAnalyticsRepository.countByRole(role);
    }

    @Override
    public long countNewSince(LocalDateTime since) {
        return userAnalyticsRepository.countNewSince(since);
    }

    @Override
    public List<Object[]> growthByMonth(LocalDateTime since) {
        return userAnalyticsRepository.growthByMonth(since);
    }

    @Override
    public List<Object[]> groupByRole() {
        return userAnalyticsRepository.groupByRole();
    }
}
