package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.seller.SellerResponseDTO;

import java.math.BigDecimal;

public interface UpdateSellerCommissionRateUseCase {
    SellerResponseDTO execute(String sellerId, BigDecimal commissionRate);
}
