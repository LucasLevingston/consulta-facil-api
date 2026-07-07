package com.consultafacil.application.port.in.commission;

import java.math.BigDecimal;

public interface HandlePaymentPaidCommissionUseCase {
    void execute(String paymentId, BigDecimal amount, String payerId);
}
