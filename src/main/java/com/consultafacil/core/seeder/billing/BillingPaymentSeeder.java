package com.consultafacil.core.seeder.billing;

import com.consultafacil.domain.entity.BillingPayment;
import com.consultafacil.domain.entity.Invoice;
import com.consultafacil.domain.enums.BillingPaymentStatus;
import com.consultafacil.domain.repository.billing.BillingPaymentRepository;
import com.consultafacil.domain.repository.billing.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingPaymentSeeder {

    private final BillingPaymentRepository billingPaymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final BillingPaymentDataProvider dataProvider;

    public void seed(String patientUserId, String professionalUserId, List<String> randomPatientIds) {
        if (billingPaymentRepository.count() > 0) return;

        List<BillingPaymentDataProvider.PaymentDef> defs =
                dataProvider.build(patientUserId, professionalUserId, randomPatientIds);

        int paymentCount = 0;
        int invoiceCount = 0;
        int seq = 1;

        for (BillingPaymentDataProvider.PaymentDef def : defs) {
            try {
                BigDecimal net = def.amount().subtract(def.systemFee());
                LocalDateTime createdAt = LocalDateTime.now().minusDays(def.daysAgo());
                LocalDateTime paidAt = def.status() == BillingPaymentStatus.PAID ? createdAt.plusMinutes(5) : null;

                BillingPayment payment = billingPaymentRepository.save(BillingPayment.builder()
                        .paymentType(def.type())
                        .ownerType(def.ownerType())
                        .ownerId(def.ownerId())
                        .amount(def.amount())
                        .systemFee(def.systemFee())
                        .gatewayFee(BigDecimal.ZERO)
                        .netAmount(net)
                        .currency("BRL")
                        .paymentMethod(def.method())
                        .gateway("MOCK")
                        .gatewayPaymentId("MOCK-SEED-" + String.format("%06d", seq))
                        .status(def.status())
                        .payerId(def.payerId())
                        .payerName(def.payerName())
                        .payerEmail(def.payerEmail())
                        .description(def.type().name().charAt(0)
                                + def.type().name().substring(1).toLowerCase()
                                + " — seed data")
                        .paidAt(paidAt)
                        .createdAt(createdAt)
                        .build());
                paymentCount++;

                if (def.status() == BillingPaymentStatus.PAID) {
                    invoiceRepository.save(Invoice.builder()
                            .payment(payment)
                            .invoiceNumber("INV-" + String.format("%06d", seq))
                            .createdAt(createdAt.plusMinutes(5))
                            .build());
                    invoiceCount++;
                }
                seq++;
            } catch (Exception e) {
                log.debug("Erro ao criar billing payment seed: {}", e.getMessage());
            }
        }
        log.info("[Seed] BillingPayments criados: {}, Invoices: {}", paymentCount, invoiceCount);
    }
}
