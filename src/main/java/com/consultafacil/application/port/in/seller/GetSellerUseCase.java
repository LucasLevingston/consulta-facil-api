package com.consultafacil.application.port.in.seller;

import com.consultafacil.api.dto.seller.SellerResponseDTO;

public interface GetSellerUseCase {
    SellerResponseDTO execute(String sellerId);
}
