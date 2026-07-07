package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.subscription.CheckoutResponseDTO;

public interface CreateCheckoutUseCase {
    CheckoutResponseDTO execute(String userId, String planId, String referralSlug, String couponCode);
}
