package com.consultafacil.domain.enums;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

/** Look-back windows accepted by the professional analytics dashboard. */
public enum AnalyticsPeriod {
    LAST_30_DAYS("30d"),
    LAST_90_DAYS("90d"),
    LAST_12_MONTHS("12m");

    private final String code;

    AnalyticsPeriod(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public LocalDateTime startFrom(LocalDateTime end) {
        return switch (this) {
            case LAST_30_DAYS -> end.minusDays(30);
            case LAST_90_DAYS -> end.minusDays(90);
            case LAST_12_MONTHS -> end.minusMonths(12);
        };
    }

    public static Optional<AnalyticsPeriod> fromCode(String code) {
        return Arrays.stream(values()).filter(p -> p.code.equalsIgnoreCase(code)).findFirst();
    }
}
