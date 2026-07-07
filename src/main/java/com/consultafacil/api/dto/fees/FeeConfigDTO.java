package com.consultafacil.api.dto.fees;

import java.math.BigDecimal;

public record FeeConfigDTO(
        BigDecimal pixFeeRate,
        BigDecimal creditCardFeeRate,
        BigDecimal debitFeeRate,
        BigDecimal platformFeeRate,
        String planSlug,
        String planName
) {}
