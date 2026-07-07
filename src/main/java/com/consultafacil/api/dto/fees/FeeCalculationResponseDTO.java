package com.consultafacil.api.dto.fees;

import java.math.BigDecimal;
import java.util.List;

public record FeeCalculationResponseDTO(
        BigDecimal amount,
        String paymentMethod,
        BigDecimal mpFeeRate,
        BigDecimal mpFeeAmount,
        BigDecimal platformFeeRate,
        BigDecimal platformFeeAmount,
        BigDecimal totalFees,
        BigDecimal professionalReceives,
        BigDecimal patientPays,
        List<PaymentMethodBreakdownDTO> comparison
) {}
