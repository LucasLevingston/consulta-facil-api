package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.seller.SellerResponseDTO;

public interface DeactivateSellerUseCase {
    SellerResponseDTO execute(String sellerId);
}
