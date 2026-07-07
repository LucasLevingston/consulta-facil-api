package com.consultafacil.application.service.tax;

import com.consultafacil.api.dto.tax.TaxBreakdown;
import com.consultafacil.core.config.TaxConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class TaxBreakdownCalculator {

    // Lucro Presumido component rates
    private static final BigDecimal LP_IRPJ   = new BigDecimal("4.80");
    private static final BigDecimal LP_CSLL   = new BigDecimal("2.88");
    private static final BigDecimal LP_PIS    = new BigDecimal("0.65");
    private static final BigDecimal LP_COFINS = new BigDecimal("3.00");
    private static final BigDecimal LP_BASE   = LP_IRPJ.add(LP_CSLL).add(LP_PIS).add(LP_COFINS);

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final TaxConfig taxConfig;

    public TaxBreakdown calculate(BigDecimal grossAmount, String paymentMethod) {
        BigDecimal feeRate = resolveProcessingFeeRate(paymentMethod);
        BigDecimal processingFee = grossAmount.multiply(feeRate)
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);

        String regime = taxConfig.getRegime();
        BigDecimal taxAmount;
        BigDecimal issAmount;
        BigDecimal taxRateApplied;

        if ("LUCRO_PRESUMIDO".equalsIgnoreCase(regime)) {
            taxRateApplied = LP_BASE;
            taxAmount = grossAmount.multiply(LP_BASE)
                    .divide(HUNDRED, 2, RoundingMode.HALF_UP);
            issAmount = grossAmount.multiply(taxConfig.getIss().getRate())
                    .divide(HUNDRED, 2, RoundingMode.HALF_UP);
        } else {
            // SIMPLES_NACIONAL — ISS included in aliquota
            taxRateApplied = taxConfig.getSimples().getRate();
            taxAmount = grossAmount.multiply(taxRateApplied)
                    .divide(HUNDRED, 2, RoundingMode.HALF_UP);
            issAmount = BigDecimal.ZERO;
        }

        BigDecimal netAmount = grossAmount.subtract(processingFee)
                .subtract(taxAmount).subtract(issAmount)
                .max(BigDecimal.ZERO);

        return new TaxBreakdown(grossAmount, processingFee, taxAmount, issAmount,
                netAmount, taxRateApplied, regime.toUpperCase(), paymentMethod);
    }

    private BigDecimal resolveProcessingFeeRate(String method) {
        if (method == null) return taxConfig.getProcessingFee().getCreditCard();
        return switch (method.toUpperCase()) {
            case "PIX"        -> taxConfig.getProcessingFee().getPix();
            case "DEBIT_CARD" -> taxConfig.getProcessingFee().getDebit();
            default           -> taxConfig.getProcessingFee().getCreditCard();
        };
    }
}
