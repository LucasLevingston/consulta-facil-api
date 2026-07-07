package com.consultafacil.application.service.billing;

import com.consultafacil.api.dto.billing.payment.BillingPaymentResponseDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CancelBillingPaymentServiceTest {

    @Mock BillingPaymentRepositoryPort paymentRepository;
    @Mock PaymentGatewayPort paymentGateway;

    CancelBillingPaymentService service;
    BillingPayment paidPayment;

    @BeforeEach
    void setUp() {
        service = new CancelBillingPaymentService(paymentRepository, paymentGateway,
                new BillingPaymentFinder(paymentRepository), new BillingPaymentMapper());

        paidPayment = BillingPayment.builder()
                .id("pay-1").gatewayPaymentId("MOCK-ABC")
                .status(BillingPaymentStatus.PAID).paidAt(LocalDateTime.now()).build();

        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void cancelPayment_found_returnsCancel() {
        when(paymentRepository.findById("pay-1")).thenReturn(Optional.of(paidPayment));
        when(paymentGateway.cancelPayment("MOCK-ABC")).thenReturn(
                BillingPayment.builder().status(BillingPaymentStatus.CANCELED).build());

        BillingPaymentResponseDTO result = service.execute("pay-1");

        assertThat(result.getStatus()).isEqualTo(BillingPaymentStatus.CANCELED);
    }

    @Test
    void cancelPayment_notFound_throws() {
        when(paymentRepository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.execute("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
