package com.consultafacil.application.service.subscription;

import com.consultafacil.api.dto.tax.TaxBreakdown;
import com.consultafacil.application.service.tax.TaxBreakdownCalculator;
import com.consultafacil.application.service.tax.TaxSnapshotBuilder;
import com.consultafacil.domain.entity.SubscriptionPayment;
import com.consultafacil.domain.port.out.subscription.SubscriptionPaymentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionPaymentRecorderTest {

    @Mock SubscriptionPaymentRepositoryPort subscriptionPaymentRepository;
    @Mock TaxBreakdownCalculator taxBreakdownCalculator;
    @Mock TaxSnapshotBuilder taxSnapshotBuilder;

    SubscriptionPaymentRecorder recorder;

    @BeforeEach
    void setUp() {
        recorder = new SubscriptionPaymentRecorder(subscriptionPaymentRepository, taxBreakdownCalculator, taxSnapshotBuilder);
    }

    private TaxBreakdown breakdown() {
        return new TaxBreakdown(new BigDecimal("99.90"), new BigDecimal("4.97"), new BigDecimal("5.99"),
                new BigDecimal("2.90"), new BigDecimal("86.04"), new BigDecimal("6.0"), "SIMPLES_NACIONAL", "CREDIT_CARD");
    }

    @Test
    void recordPayment_persistsTaxBreakdownAndSnapshot() {
        when(subscriptionPaymentRepository.existsByMpPaymentId("mp-1")).thenReturn(false);
        TaxBreakdown tax = breakdown();
        when(taxBreakdownCalculator.calculate(new BigDecimal("99.90"), "CREDIT_CARD")).thenReturn(tax);
        when(taxSnapshotBuilder.buildSnapshot(tax)).thenReturn("{\"regime\":\"SIMPLES_NACIONAL\"}");

        recorder.recordPayment("sub-1", "mp-1", new BigDecimal("99.90"), "CREDIT_CARD");

        ArgumentCaptor<SubscriptionPayment> saved = ArgumentCaptor.forClass(SubscriptionPayment.class);
        verify(subscriptionPaymentRepository).save(saved.capture());
        SubscriptionPayment payment = saved.getValue();
        assertThat(payment.getSubscriptionId()).isEqualTo("sub-1");
        assertThat(payment.getMpPaymentId()).isEqualTo("mp-1");
        assertThat(payment.getGrossAmount()).isEqualByComparingTo("99.90");
        assertThat(payment.getProcessingFee()).isEqualByComparingTo("4.97");
        assertThat(payment.getTaxAmount()).isEqualByComparingTo("5.99");
        assertThat(payment.getIssAmount()).isEqualByComparingTo("2.90");
        assertThat(payment.getNetAmount()).isEqualByComparingTo("86.04");
        assertThat(payment.getTaxRegime()).isEqualTo("SIMPLES_NACIONAL");
        assertThat(payment.getPaymentMethod()).isEqualTo("CREDIT_CARD");
        assertThat(payment.getTaxSnapshot()).isEqualTo("{\"regime\":\"SIMPLES_NACIONAL\"}");
    }

    @Test
    void recordPayment_skipsAlreadyRecordedMercadoPagoPayment() {
        when(subscriptionPaymentRepository.existsByMpPaymentId("mp-1")).thenReturn(true);

        recorder.recordPayment("sub-1", "mp-1", new BigDecimal("99.90"), "PIX");

        verify(subscriptionPaymentRepository, never()).save(any());
        verifyNoInteractions(taxBreakdownCalculator, taxSnapshotBuilder);
    }

    @Test
    void recordPayment_neverPropagatesFailures() {
        when(subscriptionPaymentRepository.existsByMpPaymentId("mp-2")).thenReturn(false);
        when(taxBreakdownCalculator.calculate(any(), any())).thenThrow(new IllegalStateException("tax config missing"));

        assertThatCode(() -> recorder.recordPayment("sub-1", "mp-2", BigDecimal.TEN, "PIX")).doesNotThrowAnyException();
        verify(subscriptionPaymentRepository, never()).save(any());
    }
}
