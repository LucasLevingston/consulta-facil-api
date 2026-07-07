package com.consultafacil.application.service.billing;

import com.consultafacil.api.dto.billing.payment.BillingPaymentResponseDTO;
import com.consultafacil.application.port.in.commission.CancelCommissionUseCase;
import com.consultafacil.domain.entity.BillingPayment;
import com.consultafacil.domain.enums.BillingPaymentStatus;
import com.consultafacil.domain.port.out.billing.BillingPaymentRepositoryPort;
import com.consultafacil.domain.port.out.billing.PaymentGatewayPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RefundBillingPaymentServiceTest {

    @Mock BillingPaymentRepositoryPort paymentRepository;
    @Mock PaymentGatewayPort paymentGateway;
    @Mock CancelCommissionUseCase cancelCommissionUseCase;

    RefundBillingPaymentService service;
    BillingPayment paidPayment;

    @BeforeEach
    void setUp() {
        service = new RefundBillingPaymentService(paymentRepository, paymentGateway, cancelCommissionUseCase,
                new BillingPaymentFinder(paymentRepository), new BillingPaymentMapper());

        paidPayment = BillingPayment.builder()
                .id("pay-1").gatewayPaymentId("MOCK-ABC").amount(new java.math.BigDecimal("100.00"))
                .status(BillingPaymentStatus.PAID).paidAt(LocalDateTime.now()).build();

        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void refundPayment_found_returnsRefund() {
        when(paymentRepository.findById("pay-1")).thenReturn(Optional.of(paidPayment));
        when(paymentGateway.refundPayment(eq("MOCK-ABC"), any())).thenReturn(
                BillingPayment.builder().status(BillingPaymentStatus.REFUNDED).build());

        BillingPaymentResponseDTO result = service.execute("pay-1");

        assertThat(result.getStatus()).isEqualTo(BillingPaymentStatus.REFUNDED);
    }
}
