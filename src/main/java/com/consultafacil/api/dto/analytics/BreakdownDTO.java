package com.consultafacil.api.dto.analytics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record BreakdownDTO(String label, long count, BigDecimal percentage) {

    public static List<BreakdownDTO> from(List<Object[]> rows) {
        long total = rows.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();
        return rows.stream().map(r -> {
            String label = r[0] != null ? r[0].toString() : "UNKNOWN";
            long count = ((Number) r[1]).longValue();
            BigDecimal pct = total == 0 ? BigDecimal.ZERO
                    : BigDecimal.valueOf(count * 100.0 / total).setScale(1, RoundingMode.HALF_UP);
            return new BreakdownDTO(label, count, pct);
        }).toList();
    }
}
