package com.consultafacil.application.service.billing;

import com.consultafacil.api.dto.billing.payment.BillingPaymentResponseDTO;
import com.consultafacil.api.dto.billing.payment.CreateBillingPaymentDTO;
import com.consultafacil.application.port.in.billing.CreateBillingPaymentUseCase;
import com.consultafacil.domain.entity.BillingPayment;
import com.consultafacil.domain.entity.Invoice;
import com.consultafacil.domain.port.out.billing.BillingPaymentRepositoryPort;
import com.consultafacil.domain.port.out.billing.InvoiceRepositoryPort;
import com.consultafacil.domain.port.out.billing.PaymentGatewayPort;
import com.consultafacil.domain.port.out.systemfee.SystemFeeRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateBillingPaymentService implements CreateBillingPaymentUseCase {

    private final BillingPaymentRepositoryPort paymentRepository;
    private final SystemFeeRepositoryPort systemFeeRepository;
    private final InvoiceRepositoryPort invoiceRepository;
    private final PaymentGatewayPort paymentGateway;
    private final BillingPaymentMapper mapper;

    @Override
    @Transactional
    public BillingPaymentResponseDTO execute(CreateBillingPaymentDTO dto) {
        BigDecimal systemFeeAmount = systemFeeRepository.findByPaymentType(dto.getPaymentType())
                .map(fee -> fee.calculate(dto.getAmount()))
                .orElse(BigDecimal.ZERO);

        BillingPayment payment = BillingPayment.builder()
                .paymentType(dto.getPaymentType())
                .referenceId(dto.getReferenceId())
                .ownerType(dto.getOwnerType())
                .ownerId(dto.getOwnerId())
                .amount(dto.getAmount())
                .systemFee(systemFeeAmount)
                .gatewayFee(BigDecimal.ZERO)
                .netAmount(dto.getAmount().subtract(systemFeeAmount))
                .currency("BRL")
                .paymentMethod(dto.getPaymentMethod())
                .gateway(dto.getGateway() != null ? dto.getGateway() : paymentGateway.gatewayName())
                .payerId(dto.getPayerId())
                .payerName(dto.getPayerName())
                .payerEmail(dto.getPayerEmail())
                .description(dto.getDescription())
                .build();

        BillingPayment saved = paymentGateway.createPayment(payment);
        saved = paymentRepository.save(saved);

        generateInvoice(saved);

        return mapper.toDTO(saved);
    }

    private void generateInvoice(BillingPayment payment) {
        String invoiceNumber = "INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Invoice invoice = Invoice.builder()
                .payment(payment)
                .invoiceNumber(invoiceNumber)
                .build();
        invoiceRepository.save(invoice);
    }
}
