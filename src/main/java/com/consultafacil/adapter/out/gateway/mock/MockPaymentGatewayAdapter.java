package com.consultafacil.adapter.out.gateway.mock;

import com.consultafacil.domain.entity.BillingPayment;
import com.consultafacil.domain.enums.BillingPaymentStatus;
import com.consultafacil.domain.port.out.PaymentGatewayPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class MockPaymentGatewayAdapter implements PaymentGatewayPort {

    @Override
    public String gatewayName() {
        return "MOCK";
    }

    @Override
    public BillingPayment createPayment(BillingPayment payment) {
        payment.setGatewayPaymentId("MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setStatus(BillingPaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        return payment;
    }

    @Override
    public BillingPayment cancelPayment(String gatewayPaymentId) {
        return BillingPayment.builder()
                .gatewayPaymentId(gatewayPaymentId)
                .status(BillingPaymentStatus.CANCELED)
                .build();
    }

    @Override
    public BillingPayment refundPayment(String gatewayPaymentId, BigDecimal amount) {
        return BillingPayment.builder()
                .gatewayPaymentId(gatewayPaymentId)
                .status(BillingPaymentStatus.REFUNDED)
                .build();
    }

    @Override
    public Map<String, Object> receiveWebhook(Map<String, Object> payload) {
        return Map.of("status", "ok", "gateway", "MOCK");
    }
}
