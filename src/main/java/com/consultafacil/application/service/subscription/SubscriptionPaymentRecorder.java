package com.consultafacil.application.service.subscription;
import com.consultafacil.application.service.tax.TaxBreakdownCalculator;
import com.consultafacil.application.service.tax.TaxSnapshotBuilder;

import com.consultafacil.api.dto.tax.TaxBreakdown;
import com.consultafacil.domain.entity.SubscriptionPayment;
import com.consultafacil.domain.port.out.subscription.SubscriptionPaymentRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionPaymentRecorder {

    private final SubscriptionPaymentRepositoryPort subscriptionPaymentRepository;
    private final TaxBreakdownCalculator taxBreakdownCalculator;
    private final TaxSnapshotBuilder taxSnapshotBuilder;

    public void recordPayment(String subscriptionId, String mpPaymentId,
                               BigDecimal grossAmount, String paymentMethod) {
        try {
            if (subscriptionPaymentRepository.existsByMpPaymentId(mpPaymentId)) {
                log.info("[Tax] Payment {} already recorded — skipping duplicate", mpPaymentId);
                return;
            }
            TaxBreakdown tax = taxBreakdownCalculator.calculate(grossAmount, paymentMethod);
            SubscriptionPayment payment = SubscriptionPayment.builder()
                    .subscriptionId(subscriptionId)
                    .mpPaymentId(mpPaymentId)
                    .grossAmount(tax.grossAmount())
                    .processingFee(tax.processingFee())
                    .taxAmount(tax.taxAmount())
                    .issAmount(tax.issAmount())
                    .netAmount(tax.netAmount())
                    .taxRateApplied(tax.taxRateApplied())
                    .taxRegime(tax.taxRegime())
                    .paymentMethod(tax.paymentMethod())
                    .taxSnapshot(taxSnapshotBuilder.buildSnapshot(tax))
                    .build();
            subscriptionPaymentRepository.save(payment);
            log.info("[Tax] Payment recorded subscriptionId={} gross={} net={}",
                    subscriptionId, grossAmount, tax.netAmount());
        } catch (Exception e) {
            log.error("[Tax] Failed to record payment for subscriptionId={}: {}", subscriptionId, e.getMessage());
        }
    }
}
