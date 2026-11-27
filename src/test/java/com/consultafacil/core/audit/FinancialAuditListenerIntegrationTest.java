package com.consultafacil.core.audit;

import com.consultafacil.ConsultaFacilApplication;
import com.consultafacil.domain.entity.BillingPayment;
import com.consultafacil.domain.entity.FinancialAuditLog;
import com.consultafacil.domain.enums.BillingPaymentStatus;
import com.consultafacil.domain.enums.FinancialAuditAction;
import com.consultafacil.domain.enums.PaymentType;
import com.consultafacil.domain.repository.audit.FinancialAuditLogRepository;
import com.consultafacil.domain.repository.billing.BillingPaymentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ConsultaFacilApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {"spring.profiles.active=test"})
class FinancialAuditListenerIntegrationTest {

    @Autowired BillingPaymentRepository billingPaymentRepository;
    @Autowired FinancialAuditLogRepository auditLogRepository;

    @AfterEach
    void cleanup() {
        auditLogRepository.deleteAll();
        billingPaymentRepository.deleteAll();
    }

    @Test
    void billingPaymentCreated_triggersCreatedAuditLog() {
        BillingPayment payment = billingPaymentRepository.save(buildPayment("150.00"));

        List<FinancialAuditLog> logs = auditLogRepository
                .findByEntityTypeAndEntityIdOrderByOccurredAtDesc("BILLING_PAYMENT", payment.getId());

        assertThat(logs).hasSize(1);
        FinancialAuditLog log = logs.get(0);
        assertThat(log.getAction()).isEqualTo(FinancialAuditAction.CREATED);
        assertThat(log.getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(log.getEntityId()).isEqualTo(payment.getId());
        assertThat(log.getActorId()).isNull();
    }

    @Test
    void billingPaymentUpdated_triggersUpdatedAuditLog() {
        BillingPayment payment = billingPaymentRepository.save(buildPayment("200.00"));

        payment.setStatus(BillingPaymentStatus.PAID);
        billingPaymentRepository.save(payment);

        List<FinancialAuditLog> logs = auditLogRepository
                .findByEntityTypeAndEntityIdOrderByOccurredAtDesc("BILLING_PAYMENT", payment.getId());

        assertThat(logs).hasSizeGreaterThanOrEqualTo(2);
        assertThat(logs).anyMatch(l -> l.getAction() == FinancialAuditAction.CREATED);
        assertThat(logs).anyMatch(l -> l.getAction() == FinancialAuditAction.UPDATED);
    }

    @Test
    void noSecurityContext_actorIdIsNull() {
        BillingPayment payment = billingPaymentRepository.save(buildPayment("75.00"));

        List<FinancialAuditLog> logs = auditLogRepository
                .findByEntityTypeAndEntityIdOrderByOccurredAtDesc("BILLING_PAYMENT", payment.getId());

        assertThat(logs).isNotEmpty();
        assertThat(logs.get(0).getActorId()).isNull();
    }

    private BillingPayment buildPayment(String amount) {
        return BillingPayment.builder()
                .paymentType(PaymentType.CONSULTATION)
                .amount(new BigDecimal(amount))
                .netAmount(new BigDecimal(amount))
                .build();
    }
}
