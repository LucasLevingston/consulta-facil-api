package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.payment.BillingPaymentResponseDTO;
import com.consultafacil.application.port.in.HandlePaymentPaidCommissionUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.BillingPayment;
import com.consultafacil.domain.enums.BillingPaymentStatus;
import com.consultafacil.domain.port.out.BillingPaymentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HandleBillingPaymentWebhookServiceTest {

    @Mock BillingPaymentRepositoryPort paymentRepository;
    @Mock HandlePaymentPaidCommissionUseCase handlePaymentPaidCommissionUseCase;

    HandleBillingPaymentWebhookService service;
    BillingPayment paidPayment;

    @BeforeEach
    void setUp() {
        service = new HandleBillingPaymentWebhookService(paymentRepository,
                handlePaymentPaidCommissionUseCase, new BillingPaymentMapper());

        paidPayment = BillingPayment.builder()
                .id("pay-1").gatewayPaymentId("MOCK-ABC").payerId("user-1")
                .status(BillingPaymentStatus.PENDING).build();

        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void handleWebhook_knownGatewayId_updatesStatus() {
        when(paymentRepository.findByGatewayPaymentId("MOCK-ABC")).thenReturn(Optional.of(paidPayment));

        BillingPaymentResponseDTO result = service.execute("MOCK-ABC", "PAID");

        assertThat(result.getStatus()).isEqualTo(BillingPaymentStatus.PAID);
    }

    @Test
    void handleWebhook_unknownGatewayId_throws() {
        when(paymentRepository.findByGatewayPaymentId("UNKNOWN")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.execute("UNKNOWN", "PAID"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
