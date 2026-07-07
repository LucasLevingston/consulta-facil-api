package com.consultafacil.api.dto.analytics;

import java.math.BigDecimal;

public record KpiDTO(String label, BigDecimal value, String unit) {
    public static KpiDTO of(String label, BigDecimal value, String unit) {
        return new KpiDTO(label, value, unit);
    }

    public static KpiDTO count(String label, long count) {
        return new KpiDTO(label, BigDecimal.valueOf(count), "count");
    }

    public static KpiDTO currency(String label, BigDecimal value) {
        return new KpiDTO(label, value != null ? value : BigDecimal.ZERO, "BRL");
    }
}
