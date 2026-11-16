package com.consultafacil.api.dto.analytics.professional;

import java.math.BigDecimal;

/** month is ISO "yyyy-MM". */
public record MonthlyRevenueDTO(String month, BigDecimal amount) {}
