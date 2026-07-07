package com.consultafacil.application.port.in.seller;

import com.consultafacil.api.dto.seller.SellerResponseDTO;

public interface DeactivateSellerUseCase {
    SellerResponseDTO execute(String sellerId);
}
