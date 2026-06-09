package com.consultafacil.application.service;

import com.consultafacil.api.dto.tax.TaxBreakdown;
import com.consultafacil.api.dto.tax.TaxReportDTO;
import com.consultafacil.core.config.TaxConfig;
import com.consultafacil.domain.entity.SubscriptionPayment;
import com.consultafacil.domain.port.out.SubscriptionPaymentRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxCalculationService {

    // Lucro Presumido component rates
    private static final BigDecimal LP_IRPJ   = new BigDecimal("4.80");
    private static final BigDecimal LP_CSLL   = new BigDecimal("2.88");
    private static final BigDecimal LP_PIS    = new BigDecimal("0.65");
    private static final BigDecimal LP_COFINS = new BigDecimal("3.00");
    private static final BigDecimal LP_BASE   = LP_IRPJ.add(LP_CSLL).add(LP_PIS).add(LP_COFINS);

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final TaxConfig taxConfig;
    private final SubscriptionPaymentRepositoryPort paymentRepository;

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

    public String buildSnapshot(TaxBreakdown b) {
        return String.format(
                "{\"regime\":\"%s\",\"rate\":%s,\"iss_rate\":%s,\"processing_fee_rate\":%s,\"payment_method\":\"%s\"}",
                b.taxRegime(),
                b.taxRateApplied().toPlainString(),
                taxConfig.getIss().getRate().toPlainString(),
                resolveProcessingFeeRate(b.paymentMethod()).toPlainString(),
                b.paymentMethod() != null ? b.paymentMethod() : "UNKNOWN");
    }

    public TaxReportDTO monthlyReport(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59);

        List<SubscriptionPayment> payments = paymentRepository.findByPaidAtBetween(start, end);

        BigDecimal totalGross = sum(payments, SubscriptionPayment::getGrossAmount);
        BigDecimal totalFees  = sum(payments, SubscriptionPayment::getProcessingFee);
        BigDecimal totalTax   = sum(payments, SubscriptionPayment::getTaxAmount);
        BigDecimal totalIss   = sum(payments, SubscriptionPayment::getIssAmount);
        BigDecimal totalNet   = sum(payments, SubscriptionPayment::getNetAmount);

        Map<String, BigDecimal> byMethod = payments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getPaymentMethod() != null ? p.getPaymentMethod() : "UNKNOWN",
                        Collectors.reducing(BigDecimal.ZERO, SubscriptionPayment::getGrossAmount, BigDecimal::add)));

        return TaxReportDTO.builder()
                .month(String.format("%d-%02d", year, month))
                .totalGross(totalGross)
                .totalProcessingFees(totalFees)
                .totalTax(totalTax)
                .totalIss(totalIss)
                .totalNet(totalNet)
                .transactionCount(payments.size())
                .taxRegime(taxConfig.getRegime().toUpperCase())
                .taxRate(taxConfig.getSimples().getRate())
                .byPaymentMethod(byMethod)
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private BigDecimal resolveProcessingFeeRate(String method) {
        if (method == null) return taxConfig.getProcessingFee().getCreditCard();
        return switch (method.toUpperCase()) {
            case "PIX"        -> taxConfig.getProcessingFee().getPix();
            case "DEBIT_CARD" -> taxConfig.getProcessingFee().getDebit();
            default           -> taxConfig.getProcessingFee().getCreditCard();
        };
    }

    private BigDecimal sum(List<SubscriptionPayment> payments,
                           java.util.function.Function<SubscriptionPayment, BigDecimal> extractor) {
        return payments.stream()
                .map(extractor)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
