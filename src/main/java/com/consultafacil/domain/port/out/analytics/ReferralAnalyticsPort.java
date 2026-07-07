package com.consultafacil.domain.port.out.analytics;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ReferralAnalyticsPort {

    long count();

    List<Object[]> countByMonth(LocalDateTime since);

    List<Object[]> groupByStatus();

    BigDecimal sumCommissions();

    long countAvailableCommissions();
}
