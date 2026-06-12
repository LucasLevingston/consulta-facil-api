package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.payment.BillingPaymentResponseDTO;
import com.consultafacil.api.dto.billing.payment.CreateBillingPaymentDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.BillingPayment;
import com.consultafacil.domain.entity.Invoice;
import com.consultafacil.domain.entity.SystemFee;
import com.consultafacil.domain.enums.BillingPaymentStatus;
import com.consultafacil.domain.enums.PaymentType;
import com.consultafacil.domain.port.out.BillingPaymentRepositoryPort;
import com.consultafacil.domain.port.out.InvoiceRepositoryPort;
import com.consultafacil.domain.port.out.PaymentGatewayPort;
import com.consultafacil.domain.port.out.SystemFeeRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BillingPaymentServiceTest {

    @Mock BillingPaymentRepositoryPort paymentRepository;
    @Mock SystemFeeRepositoryPort systemFeeRepository;
    @Mock InvoiceRepositoryPort invoiceRepository;
    @Mock PaymentGatewayPort paymentGateway;

    @InjectMocks BillingPaymentService service;

    BillingPayment paidPayment;

    @BeforeEach
    void setUp() {
        paidPayment = BillingPayment.builder()
                .id("pay-1")
                .paymentType(PaymentType.CONSULTATION)
                .amount(new BigDecimal("100.00"))
                .systemFee(new BigDecimal("5.00"))
                .gatewayFee(BigDecimal.ZERO)
                .netAmount(new BigDecimal("95.00"))
                .currency("BRL")
                .gateway("MOCK")
                .gatewayPaymentId("MOCK-ABC")
                .status(BillingPaymentStatus.PAID)
                .payerId("user-1")
                .paidAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentGateway.gatewayName()).thenReturn("MOCK");
        when(paymentGateway.createPayment(any())).thenAnswer(inv -> {
            BillingPayment p = inv.getArgument(0);
            p.setGatewayPaymentId("MOCK-XYZ");
            p.setStatus(BillingPaymentStatus.PAID);
            p.setPaidAt(LocalDateTime.now());
            return p;
        });
    }

    // ── createPayment ────────────────────────────────────────────────────

    @Test
    void createPayment_withFee_deductsSystemFee() {
        SystemFee fee = SystemFee.builder()
                .paymentType(PaymentType.CONSULTATION)
                .fixedFee(new BigDecimal("5.00"))
                .percentageFee(BigDecimal.ZERO)
                .active(true)
                .build();
        when(systemFeeRepository.findByPaymentType(PaymentType.CONSULTATION)).thenReturn(Optional.of(fee));

        CreateBillingPaymentDTO dto = new CreateBillingPaymentDTO();
        dto.setPaymentType(PaymentType.CONSULTATION);
        dto.setAmount(new BigDecimal("100.00"));
        dto.setPayerId("user-1");

        BillingPaymentResponseDTO result = service.createPayment(dto);

        assertThat(result.getSystemFee()).isEqualByComparingTo("5.00");
        assertThat(result.getNetAmount()).isEqualByComparingTo("95.00");
        assertThat(result.getStatus()).isEqualTo(BillingPaymentStatus.PAID);
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void createPayment_noFeeConfig_zeroSystemFee() {
        when(systemFeeRepository.findByPaymentType(any())).thenReturn(Optional.empty());

        CreateBillingPaymentDTO dto = new CreateBillingPaymentDTO();
        dto.setPaymentType(PaymentType.EXAM);
        dto.setAmount(new BigDecimal("50.00"));
        dto.setPayerId("user-2");

        BillingPaymentResponseDTO result = service.createPayment(dto);

        assertThat(result.getSystemFee()).isEqualByComparingTo("0.00");
        assertThat(result.getNetAmount()).isEqualByComparingTo("50.00");
    }

    // ── cancelPayment ────────────────────────────────────────────────────

    @Test
    void cancelPayment_found_returnsCancel() {
        when(paymentRepository.findById("pay-1")).thenReturn(Optional.of(paidPayment));
        when(paymentGateway.cancelPayment("MOCK-ABC")).thenReturn(
                BillingPayment.builder().status(BillingPaymentStatus.CANCELED).build());

        BillingPaymentResponseDTO result = service.cancelPayment("pay-1");

        assertThat(result.getStatus()).isEqualTo(BillingPaymentStatus.CANCELED);
    }

    @Test
    void cancelPayment_notFound_throws() {
        when(paymentRepository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.cancelPayment("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── refundPayment ────────────────────────────────────────────────────

    @Test
    void refundPayment_found_returnsRefund() {
        when(paymentRepository.findById("pay-1")).thenReturn(Optional.of(paidPayment));
        when(paymentGateway.refundPayment(eq("MOCK-ABC"), any())).thenReturn(
                BillingPayment.builder().status(BillingPaymentStatus.REFUNDED).build());

        BillingPaymentResponseDTO result = service.refundPayment("pay-1");

        assertThat(result.getStatus()).isEqualTo(BillingPaymentStatus.REFUNDED);
    }

    // ── handleWebhook ────────────────────────────────────────────────────

    @Test
    void handleWebhook_knownGatewayId_updatesStatus() {
        paidPayment.setStatus(BillingPaymentStatus.PENDING);
        paidPayment.setPaidAt(null);
        when(paymentRepository.findByGatewayPaymentId("MOCK-ABC")).thenReturn(Optional.of(paidPayment));

        BillingPaymentResponseDTO result = service.handleWebhook("MOCK-ABC", "PAID");

        assertThat(result.getStatus()).isEqualTo(BillingPaymentStatus.PAID);
    }

    @Test
    void handleWebhook_unknownGatewayId_throws() {
        when(paymentRepository.findByGatewayPaymentId("UNKNOWN")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.handleWebhook("UNKNOWN", "PAID"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── listMyPayments ───────────────────────────────────────────────────

    @Test
    void listMyPayments_returnsList() {
        when(paymentRepository.findByPayerId("user-1")).thenReturn(List.of(paidPayment));
        assertThat(service.listMyPayments("user-1")).hasSize(1);
    }

    @Test
    void listAll_returnsList() {
        when(paymentRepository.findAll()).thenReturn(List.of(paidPayment));
        assertThat(service.listAll()).hasSize(1);
    }
}
