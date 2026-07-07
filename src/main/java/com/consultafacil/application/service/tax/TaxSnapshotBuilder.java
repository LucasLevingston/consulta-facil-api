package com.consultafacil.application.service.tax;

import com.consultafacil.api.dto.tax.TaxBreakdown;
import com.consultafacil.core.config.TaxConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TaxSnapshotBuilder {

    private final TaxConfig taxConfig;

    public String buildSnapshot(TaxBreakdown b) {
        return String.format(
                "{\"regime\":\"%s\",\"rate\":%s,\"iss_rate\":%s,\"processing_fee_rate\":%s,\"payment_method\":\"%s\"}",
                b.taxRegime(),
                b.taxRateApplied().toPlainString(),
                taxConfig.getIss().getRate().toPlainString(),
                resolveProcessingFeeRate(b.paymentMethod()).toPlainString(),
                b.paymentMethod() != null ? b.paymentMethod() : "UNKNOWN");
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
