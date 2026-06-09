package com.consultafacil.api.dto.tax;

import java.math.BigDecimal;

public record TaxBreakdown(
        BigDecimal grossAmount,
        BigDecimal processingFee,
        BigDecimal taxAmount,
        BigDecimal issAmount,
        BigDecimal netAmount,
        BigDecimal taxRateApplied,
        String taxRegime,
        String paymentMethod
) {}
