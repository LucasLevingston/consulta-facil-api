package com.consultafacil.application.port.in;

import com.consultafacil.api.dto.seller.SellerResponseDTO;

public interface GetSellerUseCase {
    SellerResponseDTO execute(String sellerId);
}
