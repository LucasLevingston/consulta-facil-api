package com.consultafacil.application.service.billing;

import com.consultafacil.api.dto.billing.payment.BillingPaymentResponseDTO;
import com.consultafacil.api.dto.billing.payment.CreateBillingPaymentDTO;
import com.consultafacil.domain.entity.Invoice;
import com.consultafacil.domain.entity.SystemFee;
import com.consultafacil.domain.enums.BillingPaymentStatus;
import com.consultafacil.domain.enums.PaymentType;
import com.consultafacil.domain.entity.BillingPayment;
import com.consultafacil.domain.port.out.billing.BillingPaymentRepositoryPort;
import com.consultafacil.domain.port.out.billing.InvoiceRepositoryPort;
import com.consultafacil.domain.port.out.billing.PaymentGatewayPort;
import com.consultafacil.domain.port.out.systemfee.SystemFeeRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateBillingPaymentServiceTest {

    @Mock BillingPaymentRepositoryPort paymentRepository;
    @Mock SystemFeeRepositoryPort systemFeeRepository;
    @Mock InvoiceRepositoryPort invoiceRepository;
    @Mock PaymentGatewayPort paymentGateway;

    CreateBillingPaymentService service;

    @BeforeEach
    void setUp() {
        service = new CreateBillingPaymentService(paymentRepository, systemFeeRepository,
                invoiceRepository, paymentGateway, new BillingPaymentMapper());

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

        BillingPaymentResponseDTO result = service.execute(dto);

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

        BillingPaymentResponseDTO result = service.execute(dto);

        assertThat(result.getSystemFee()).isEqualByComparingTo("0.00");
        assertThat(result.getNetAmount()).isEqualByComparingTo("50.00");
    }
}
