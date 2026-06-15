package com.consultafacil.api.dto.fees;

import java.math.BigDecimal;

public record PaymentMethodBreakdownDTO(
        String paymentMethod,
        BigDecimal mpFeeRate,
        BigDecimal mpFeeAmount,
        BigDecimal platformFeeAmount,
        BigDecimal totalFees,
        BigDecimal professionalReceives,
        BigDecimal patientPays
) {}
