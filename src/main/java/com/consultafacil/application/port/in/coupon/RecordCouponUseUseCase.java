package com.consultafacil.application.port.in.coupon;

import java.math.BigDecimal;

public interface RecordCouponUseUseCase {
    void execute(String couponId, String userId, String subscriptionId, BigDecimal discountApplied);
}
