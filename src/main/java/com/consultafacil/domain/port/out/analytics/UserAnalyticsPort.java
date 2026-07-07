package com.consultafacil.domain.port.out.analytics;

import com.consultafacil.domain.enums.UserRole;

import java.time.LocalDateTime;
import java.util.List;

public interface UserAnalyticsPort {

    long count();

    long countByRole(UserRole role);

    long countNewSince(LocalDateTime since);

    List<Object[]> growthByMonth(LocalDateTime since);

    List<Object[]> groupByRole();
}
