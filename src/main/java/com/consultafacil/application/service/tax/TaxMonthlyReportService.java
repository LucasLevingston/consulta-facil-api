package com.consultafacil.application.service.tax;

import com.consultafacil.api.dto.tax.TaxReportDTO;
import com.consultafacil.application.port.in.TaxMonthlyReportUseCase;
import com.consultafacil.core.config.TaxConfig;
import com.consultafacil.domain.entity.SubscriptionPayment;
import com.consultafacil.domain.port.out.SubscriptionPaymentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaxMonthlyReportService implements TaxMonthlyReportUseCase {

    private final TaxConfig taxConfig;
    private final SubscriptionPaymentRepositoryPort paymentRepository;

    @Override
    public TaxReportDTO execute(int year, int month) {
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

    private BigDecimal sum(List<SubscriptionPayment> payments, Function<SubscriptionPayment, BigDecimal> extractor) {
        return payments.stream()
                .map(extractor)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
