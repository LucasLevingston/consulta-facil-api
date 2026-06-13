package com.consultafacil.application.service;

import com.consultafacil.api.dto.billing.payment.BillingPaymentResponseDTO;
import com.consultafacil.api.dto.billing.payment.CreateBillingPaymentDTO;
import com.consultafacil.application.port.in.BillingPaymentUseCase;
import com.consultafacil.application.port.in.CommissionUseCase;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.BillingPayment;
import com.consultafacil.domain.entity.Invoice;
import com.consultafacil.domain.enums.BillingPaymentStatus;
import com.consultafacil.domain.port.out.BillingPaymentRepositoryPort;
import com.consultafacil.domain.port.out.InvoiceRepositoryPort;
import com.consultafacil.domain.port.out.PaymentGatewayPort;
import com.consultafacil.domain.port.out.SystemFeeRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillingPaymentService implements BillingPaymentUseCase {

    private final BillingPaymentRepositoryPort paymentRepository;
    private final SystemFeeRepositoryPort systemFeeRepository;
    private final InvoiceRepositoryPort invoiceRepository;
    private final PaymentGatewayPort paymentGateway;
    private final CommissionUseCase commissionUseCase;

    @Override
    @Transactional
    public BillingPaymentResponseDTO createPayment(CreateBillingPaymentDTO dto) {
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

        return toDTO(saved);
    }

    @Override
    @Transactional
    public BillingPaymentResponseDTO cancelPayment(String id) {
        BillingPayment payment = findOrThrow(id);
        BillingPayment cancelled = paymentGateway.cancelPayment(payment.getGatewayPaymentId());
        payment.setStatus(cancelled.getStatus());
        return toDTO(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public BillingPaymentResponseDTO refundPayment(String id) {
        BillingPayment payment = findOrThrow(id);
        BillingPayment refunded = paymentGateway.refundPayment(payment.getGatewayPaymentId(), payment.getAmount());
        payment.setStatus(refunded.getStatus());
        BillingPayment saved = paymentRepository.save(payment);
        commissionUseCase.cancelCommission(saved.getId());
        return toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BillingPaymentResponseDTO getById(String id) {
        return toDTO(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillingPaymentResponseDTO> listMyPayments(String payerId) {
        return paymentRepository.findByPayerId(payerId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillingPaymentResponseDTO> listAll() {
        return paymentRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BillingPaymentResponseDTO handleWebhook(String gatewayPaymentId, String newStatus) {
        BillingPayment payment = paymentRepository.findByGatewayPaymentId(gatewayPaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("BillingPayment", gatewayPaymentId));
        try {
            payment.setStatus(BillingPaymentStatus.valueOf(newStatus.toUpperCase()));
        } catch (IllegalArgumentException ignored) {
        }
        boolean wasPending = payment.getStatus() != BillingPaymentStatus.PAID;
        if (payment.getStatus() == BillingPaymentStatus.PAID && payment.getPaidAt() == null) {
            payment.setPaidAt(LocalDateTime.now());
        }
        BillingPayment saved = paymentRepository.save(payment);
        if (wasPending && saved.getStatus() == BillingPaymentStatus.PAID && saved.getPayerId() != null) {
            commissionUseCase.onPaymentPaid(saved.getId(), saved.getAmount(), saved.getPayerId());
        }
        return toDTO(saved);
    }

    private void generateInvoice(BillingPayment payment) {
        String invoiceNumber = "INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Invoice invoice = Invoice.builder()
                .payment(payment)
                .invoiceNumber(invoiceNumber)
                .build();
        invoiceRepository.save(invoice);
    }

    private BillingPayment findOrThrow(String id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BillingPayment", id));
    }

    private BillingPaymentResponseDTO toDTO(BillingPayment p) {
        return BillingPaymentResponseDTO.builder()
                .id(p.getId())
                .paymentType(p.getPaymentType())
                .referenceId(p.getReferenceId())
                .ownerType(p.getOwnerType())
                .ownerId(p.getOwnerId())
                .amount(p.getAmount())
                .systemFee(p.getSystemFee())
                .gatewayFee(p.getGatewayFee())
                .netAmount(p.getNetAmount())
                .currency(p.getCurrency())
                .paymentMethod(p.getPaymentMethod())
                .gateway(p.getGateway())
                .gatewayPaymentId(p.getGatewayPaymentId())
                .status(p.getStatus())
                .payerId(p.getPayerId())
                .payerName(p.getPayerName())
                .payerEmail(p.getPayerEmail())
                .description(p.getDescription())
                .paidAt(p.getPaidAt())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
