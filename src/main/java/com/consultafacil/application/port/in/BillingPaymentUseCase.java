package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.billing.payment.BillingPaymentResponseDTO;
import com.consultafacil.api.dto.billing.payment.CreateBillingPaymentDTO;

import java.util.List;

public interface BillingPaymentUseCase {
    BillingPaymentResponseDTO createPayment(CreateBillingPaymentDTO dto);
    BillingPaymentResponseDTO cancelPayment(String id);
    BillingPaymentResponseDTO refundPayment(String id);
    BillingPaymentResponseDTO getById(String id);
    List<BillingPaymentResponseDTO> listMyPayments(String payerId);
    List<BillingPaymentResponseDTO> listAll();
    BillingPaymentResponseDTO handleWebhook(String gatewayPaymentId, String newStatus);
}
