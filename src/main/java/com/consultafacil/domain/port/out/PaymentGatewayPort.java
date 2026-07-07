package com.consultafacil.domain.port.out;

import com.consultafacil.domain.entity.BillingPayment;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentGatewayPort {

    String gatewayName();

    BillingPayment createPayment(BillingPayment payment);

    BillingPayment cancelPayment(String gatewayPaymentId);

    BillingPayment refundPayment(String gatewayPaymentId, BigDecimal amount);

    Map<String, Object> receiveWebhook(Map<String, Object> payload);
}
